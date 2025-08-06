[정산 데이터, 이제 스트리밍으로 즐기세요! (feat. Kafka streams) 1편](https://data-make.tistory.com/802)에 이어 2편을 진행하겠습니다.
<br/>

### 5단계. 지급룰 조회 및 세팅

![지급룰 조회 및 세팅](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-processValues.png?raw=true)

이제 필터링된 정산 대상 데이터에 지급룰 정보를 설정할 차례입니다. 지급룰은 API 호출을 통해 조회하는데, 동일한 규칙을 사용하는 데이터에 대해 중복 API 호출을 방지하고 네트워크 통신 비용을 절감하기 위해 지급룰을 별도로 관리하고자 합니다.
<br/>

이러한 요구사항을 해결하기 위해 `Redis`를 사용할 수도 있지만, 여기서는 Kafka Streams의 `상태 저장소`를 활용해 보겠습니다. 상태 저장소는 `RocksDB`와 같은 로컬 저장소를 사용하여 `KTable` 형태로 키-값 데이터를 관리하며, `변경 로그 토픽`을 통해 상태를 복원하여 내결함성을 보장합니다. 이렇게 구성된 상태 저장소와 연동하여 레코드를 개별적으로 처리하기 위해 
[processValues()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#processValues-org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier-java.lang.String...-) 메서드를 사용합니다.
<br/>

`processValues()` 메서드는 스트림의 각 레코드에 대해 키는 그대로 유지하면서 값만을 대상으로 사용자 정의 로직을 실행할 때 유용합니다. 이 사용자 정의 로직은 `FixedKeyProcessorSupplier` 인터페이스를 구현한 객체를 processValues() 메서드의 인자로 전달하여 정의합니다

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    builder.globalTable( // 토폴로지에 GlobalKTable 정의
        kafkaProperties.paymentRulesGlobalTopic, //  GlobalKTable이 데이터를 읽어올 토픽 이름
        // 상태 저장소 설정
        Materialized.`as`<String, Rule, KeyValueStore<Bytes, ByteArray>>( // GlobalKTable이 String 키와 Rule 값을 가지며, 내부적으로 KeyValueStore 타입의 상태 저장소를 사용할 것임을 명시
            kafkaProperties.globalPayoutRuleStateStoreName // 내부 상태 저장소에 부여하는 고유한 이름
        )
            .withKeySerde(Serdes.String()) // GlobalKTable의 소스 토픽에서 레코드를 읽을 때 키를 역직렬화하고, 내부 상태 저장소에 키를 직렬화/역직렬화할 때 사용
            .withValueSerde(serdeFactory.ruleSerde()) // GlobalKTable의 소스 토픽에서 레코드를 읽을 때 값을 역직렬화하고, 내부 상태 저장소에 값을 직렬화/역직렬화할 때 사용
    )

    // ...
    paymentStream
        .processValues(
            // 사용자 정의 상태 기반 값 처리 로직을 적용
            PayoutRuleProcessValues(
                rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
                stateStoreName = kafkaProperties.globalPayoutRuleStateStoreName,
                payoutRuleClient = payoutRuleClient,
                ruleKafkaTemplate = ruleKafkaTemplate,
            ),
        )
    // ...
}
```

`FixedKeyProcessorSupplier` 인터페이스 구현체

```kotlin
class PayoutRuleProcessValues(
  private val rulesGlobalTopic: String, // GlobalKTable의 소스 토픽 이름
  private val stateStoreName: String, // GlobalKTable의 로컬 상태 저장소 이름
  private val payoutRuleClient: PayoutRuleClient, // 외부 API 호출을 위한 클라이언트
  private val ruleKafkaTemplate: KafkaTemplate<String, Rule>, // 지급룰 정보를 토픽으로 보내기 위한 카프카 템플릿
) : FixedKeyProcessorSupplier<String, Base, Base> {
  override fun get(): FixedKeyProcessor<String, Base, Base> {
    return PayoutRuleProcessor(rulesGlobalTopic, stateStoreName, payoutRuleClient, ruleKafkaTemplate)
  }
}

class PayoutRuleProcessor(
  private val rulesGlobalTopic: String,
  private val stateStoreName: String,
  private val payoutRuleClient: PayoutRuleClient,
  private val ruleKafkaTemplate: KafkaTemplate<String, Rule>,
) : FixedKeyProcessor<String, Base, Base> {
  private var context: FixedKeyProcessorContext<String, Base>? = null
  private var payoutRuleStore: ReadOnlyKeyValueStore<String, ValueAndTimestamp<Rule>>? = null

  // 초기화 메서드
  override fun init(context: FixedKeyProcessorContext<String, Base>) {
    this.context = context
    this.payoutRuleStore = this.context?.getStateStore(stateStoreName) // 상태 저장소 이름을 통해 GlobalKTable의 로컬 복제본에 접근
  }

  // 레코드 처리 메서드
  override fun process(record: FixedKeyRecord<String, Base>) {
    val key = record.key()
    val base = record.value()

    // 결제 데이터가 없을 경우 스킵
    if (base == null) {
      log.info(">>> [결제 데이터 누락] Payment data is null, skipping processing for key: $key")
      return
    }

    // 지급룰 조회를 위한 키 생성 (가맹점번호/결제일/결제액션타입/결제수단타입)
    val ruleKey = "${base.merchantNumber}/${base.paymentDate.toLocalDate()}/${base.paymentActionType}/${base.paymentMethodType}"
    // 상태 저장소(GlobalKTable)에서 ruleKey로 지급룰 조회
    val valueAndTimestamp = payoutRuleStore?.get(ruleKey)
    var rule = valueAndTimestamp?.value()
    // 상태 저장소에 지급룰이 없을 경우
    if (rule == null) {
      // 외부 API를 통해 지급룰 조회
      val findRule = payoutRuleClient.getPayoutDate(
        PayoutDateRequest(
          merchantNumber = base.merchantNumber ?: throw IllegalArgumentException(),
          paymentDate = base.paymentDate,
          paymentActionType = base.paymentActionType ?: throw IllegalArgumentException(),
          paymentMethodType = base.paymentMethodType ?: throw IllegalArgumentException(),
        )
      )
      // 조회된 지급룰을 GlobalKTable의 소스 토픽으로 전송하여 GlobalKTable이 업데이트되고, 다른 인스턴스에서도 이 지급룰을 사용
      ruleKafkaTemplate.send(rulesGlobalTopic, ruleKey, findRule)
      rule = findRule
    }

    // 가맹점에 대한 지급룰이 없을 경우
    if (rule == null) {
      log.info(">>> [지급룰 없음] Not found payment payout rule. key: $ruleKey")
      base.updateDefaultPayoutDate()
    }

    // 지급룰 업데이트 대상일 경우
    if (rule != null && (rule.payoutDate != base.payoutDate || rule.confirmDate != base.confirmDate)) {
      log.info(">>> [지급룰 업데이트] Update payout date.. $ruleKey")
      base.updatePayoutDate(rule)
    }

    // 처리된 Base 객체를 다음 스트림 단계로 전달
    context?.forward(record.withValue(base))
  }

  companion object {
    private val log by logger()
  }
}
```

상태 저장소에 저장된 데이터는 `변경 로그 토픽`을 통해 복원됩니다. `GlobalKTable`의 경우, 정의 시 지정했던 소스 토픽 자체가 변경 로그 토픽 역할을 합니다. 해당 토픽에서 지급룰 정보의 변경 내역을 확인할 수 있습니다

```text
[key]
merchant-4436/2025-05-26/PAYMENT/MONEY

[value]
{
   "ruleId": "16858a4e-d08c-4ba5-ae44-54ff4d4b219b",
   "payoutDate": "2025-06-10",
   "confirmDate": "2025-06-09",
   "merchantNumber": "merchant-4436",
   "paymentDate": "2025-05-26T00:00:00",
   "paymentActionType": "PAYMENT",
   "paymentMethodType": "MONEY"
}
```

💡**상태 저장소를 사용하는 방법**

Kafka Streams에서 상태를 관리하는 주요 방법으로 `KTable`과 `GlobalKTable`이 있습니다. 두 방식의 주요 차이점을 살펴보겠습니다.

|                  | KTable                                                                                                            | GlobalKTable                                                                                              |
| ---------------- | ----------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| 데이터 저장 방식 | 데이터 분산 저장 (각 파티션의 데이터는 해당 파티션을 담당하는 인스턴스에 저장)                                    | 모든 인스턴스의 로컬 상태 저장소에 전체 데이터 복제                                                       |
| 데이터 조회      | 특정 키 조회 시, 해당 키가 속한 파티션을 알아야 하며, 데이터가 다른 인스턴스에 있다면 네트워크를 통한 조회가 필요 | 각 인스턴스가 전체 데이터의 로컬 복사본을 가지므로, 파티션에 관계없이 어떤 키든 로컬에서 빠르게 조회 가능 |
| 변경 로그        | 소스 토픽의 파티션과 1:1로 매핑되는 별도의 내부 변경 로그 토픽을 생성하고 사용                                    | 소스 토픽 자체가 변경 로그 역할을 하며, 모든 인스턴스가 이 토픽을 구독하여 로컬 상태를 최신으로 유지      |

<br/>

이 글에서는 지급룰을 상태 저장소에 저장하기 위해 `GlobalKTable`을 사용했습니다. `GlobalKTable`은 `KTable`과 달리 모든 인스턴스가 동일한 소스 토픽을 변경 로그로 사용합니다. 따라서 각 인스턴스는 소스 토픽에서 데이터를 읽어와 자신의 로컬 상태 저장소를 업데이트하고, 이를 통해 모든 인스턴스가 전체 데이터셋의 완전한 복제본을 로컬에 유지하게 됩니다. 그 결과, 파티션에 관계없이 로컬에서 신속한 조회가 가능해집니다.
<br/>

반면, `KTable`은 각 인스턴스가 자신이 담당하는 파티션의 변경 로그만을 소비하여 로컬 상태를 관리합니다. 이로 인해 데이터가 파티션별로 분산 저장되어, 특정 키를 조회할 때 해당 키가 어떤 파티션에 있는지 확인해야 하는 번거로움이 있을 수 있습니다. `Interactive Queries`를 사용하면 특정 키를 담당하는 인스턴스의 정보를 얻어 HTTP 요청을 통해 데이터를 가져올 수 있지만, 지급룰 조회와 같이 빈번한 읽기 작업에는 불필요한 네트워크 통신이 발생할 수 있습니다. 이러한 이유로 지급룰 데이터를 캐시처럼 활용하기 위해 `GlobalKTable`을 선택했습니다.

### 6단계. 정산 베이스 저장

![정산 베이스 저장](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-peek-2.png?raw=true)

'2단계. 결제 메시지 저장' 단계에서 사용되었던 [peek()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#peek-org.apache.kafka.streams.kstream.ForeachAction-) 메서드를 활용해서 정산 베이스를 데이터베이스에 저장합니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    paymentStream
        .peek({ _, message -> settlementService.saveBase(message) })
    // ...
}
```

### 7단계. 집계

![집계](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-aggregate.png?raw=true)

정산 베이스 데이터에 대한 통계를 생성하기 위해 스트림 레코드를 집계합니다. 먼저, [groupBy()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KTable.html#groupBy-org.apache.kafka.streams.kstream.KeyValueMapper-) 메서드를 사용하여 집계 기준이 될 키와 값을 스트림 레코드에서 추출하여 `KGroupedStream`을 생성합니다. 그런 다음, 이 `KGroupedStream`에 [aggregate()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KGroupedStream.html#aggregate-org.apache.kafka.streams.kstream.Initializer-org.apache.kafka.streams.kstream.Aggregator-org.apache.kafka.streams.kstream.Materialized-) 메서드를 적용하여 그룹화된 키를 기준으로 레코드 값을 집계합니다. 집계 결과는 `KTable` 형태로 저장되며, 이는 '5단계. 지급룰 조회 및 세팅'에서 언급된 것처럼 `Interactive Queries`를 통해 조회할 수 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    val aggregatedTable: KTable<BaseAggregationKey, BaseAggregateValue> = baseStream.groupBy( // baseStream을 그룹화하고 집계하여 KTable을 생성
        { _, base -> base.toAggregationKey() }, // 각 Base 객체에서 집계에 사용할 키 추출
        Grouped.with( // 그룹화 연산 시 사용할 키와 값의 직렬화/역직렬화기(Serde) 설정
            serdeFactory.baseAggregationKeySerde(), // 그룹화 키를 위한 Serde
            serdeFactory.baseSerde() // 그룹화될 값을 위한 Serde
        )
    )
        .aggregate( // 그룹화된 스트림(KGroupedStream)에 대해 집계 연산을 수행
            { BaseAggregateValue() }, // 새로운 그룹이 생성될 때 사용할 초기 집계값 정의
            { _aggKey, newBaseValue, currentAggregate -> // (그룹 키, 새로운 값, 현재 집계값) -> 새로운 집계값
                currentAggregate.updateWith(newBaseValue.amount) // 각 그룹에 새로운 레코드가 도착할 때마다 현재 집계값을 업데이트
            },
            // 집계 결과를 상태 저장소에 저장하기 위한 설정 정의
            Materialized.`as`<BaseAggregationKey, BaseAggregateValue, KeyValueStore<Bytes, ByteArray>>(
                kafkaProperties.statisticsStoreName // 상태 저장소 이름 정의
            )
                .withKeySerde(serdeFactory.baseAggregationKeySerde()) // 상태 저장소 및 집계 결과 KTable의 키를 위한 Serde
                .withValueSerde(serdeFactory.baseAggregateValueSerde()) // 상태 저장소 및 집계 결과 KTable의 값을 위한 Serde
        )
    // ...
}

// Base.kt (집계에 사용할 키 정의)
fun toAggregationKey() = 
  BaseAggregationKey(
    merchantNumber = this.merchantNumber,
    paymentDateDaily = this.paymentDate.toLocalDate(),
    paymentActionType = this.paymentActionType,
    paymentMethodType = this.paymentMethodType
  )

// BaseAggregateValue.kt (초기 집계값 및 집계 계산식 정의)
data class BaseAggregateValue(
  val totalAmount: Long = 0L,
  val count: Long = 0L
) {
  fun updateWith(amount: Long): BaseAggregateValue {
    return this.copy(
      totalAmount = this.totalAmount + amount,
      count = this.count + 1
    )
  }
}
```

### 8단계. 집계 결과 전송

![집계 결과 전송](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-to.png?raw=true)

집계 결과인 `KTable`은 [toStream()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KTable.html#toStream--) 메서드를 통해 `KStream`으로 변환한 후, 싱크 프로세서인 [to()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#to-java.lang.String-org.apache.kafka.streams.kstream.Produced-) 메서드를 사용하여 다른 Kafka 토픽으로 전송할 수 있습니다. 물론 `Interactive Queries`를 활용하여 집계 결과를 직접 조회하는 것으로 마무리할 수도 있지만, 결과를 토픽으로 전송하면 데이터 변경 이력을 남기거나 집계 결과를 검증하는 데 유용하게 활용할 수 있습니다.

```kotlin
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    aggregatedTable.toStream() // 집계 결과인 KTable을 KStream으로 변환
        .to( // KStream의 데이터를 특정 토픽으로 전송
            kafkaProperties.paymentStatisticsTopic, // 데이터를 전송할 대상 토픽 이름
            Produced.with(
                serdeFactory.baseAggregationKeySerde(),
                serdeFactory.baseAggregateValueSerde()
            )
        )
    // ...
}
```

## kafka streams 인스턴스 생성

KafkaStreams 인스턴스는 [start()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/KafkaStreams.html#start--) 메서드를 호출하여 시작할 수 있습니다. 이 글에서는 KafkaStreams를 Spring Bean으로 등록하고, 별도의 CommandLineRunner를 통해 애플리케이션 시작 시 자동으로 실행되도록 구현했습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    return KafkaStreams(builder.build(), streamsConfig)
}

// KafkaStreamsRunner.kt
@Component
class KafkaStreamsRunner(
  private val settlementKafkaStreamsApp: SettlementKafkaStreamsApp,
) : CommandLineRunner {

    private lateinit var settlementStreams: KafkaStreams
  
    override fun run(vararg args: String?) {
        // ...
        settlementStreams.start()
        return
    }
    // ...
}
```

## 마치며

이 글을 통해 Kafka Streams를 활용하여 기존의 비실시간 정산 데이터 처리 과정을 실시간 스트리밍 방식으로 전환하는 여정을 함께 살펴보았습니다. Kafka를 이미 사용하고 계시다면, 별도의 복잡한 클러스터 구축 없이도 강력한 스트림 처리 기능을 애플리케이션에 통합할 수 있다는 점이 Kafka Streams의 큰 매력입니다. 단순한 데이터 필터링부터 상태 기반의 복잡한 연산, 그리고 데이터 집계에 이르기까지, Kafka Streams는 다양한 기능을 직관적인 DSL로 제공하여 개발 과정을 한결 수월하게 만들어 줍니다. 특히, GlobalKTable과 같은 상태 저장소 활용은 외부 시스템 의존도를 낮추고 실시간 조회 성능을 극대화하는 데 큰 도움이 되었습니다. 만약 여러분의 시스템에서도 대용량 데이터를 실시간으로 처리해야 하는 도전 과제가 있다면, 혹은 기존 Kafka 인프라를 더욱 효과적으로 활용하고 싶다면, Kafka Streams 도입을 적극적으로 고려해 보시길 권합니다. 
<br/>

이 글에서 다룬 예제가 여러분의 스트리밍 여정에 작은 영감이 되기를 바랍니다. 지금 바로 Kafka Streams의 강력함을 경험해 보세요!

## 참고
- [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html)
- [Kafka Streams Architecture for Confluent Platform](https://docs.confluent.io/platform/current/streams/architecture.html)
- [Kafka Streams Interactive Queries for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/interactive-queries.html)

