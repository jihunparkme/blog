# 스트림하게 정산 데이터 생성하기

정산플랫폼팀에서 결제 데이터를 기반으로 정산에 필요한 정산 데이터를 생성하는 역할을 맡고 있습니다.

결제팀으로부터 매일 수백만 건 이상의 결제 데이터가 카프카(`kafka`)를 통해 전달되고 있는데, 최근 업무에 파이프라인 방식으로 데이터를 처리하기 위해 카프카 스트림즈(`kafka streams`)를 적용해 보며,<br/>
"기존에 비실시간으로 생성되던 정산 데이터를 `카프카 스트림즈`를 적용하여 실시간으로 스트림하게 생성되도록 해보면 어떨까?"라는 생각을 하게 되었습니다.

기존 카프카를 사용 중이었다면 별도의 클러스터 구성 없이 자체 인프라를 활용하여 간편하게 스트림 처리 애플리케이션을 구축하도록 도와주는 `카프카 스트림즈`의 매력을<br/>
간단한 정산 데이터 생성 예시와 함께 공유해 보려고 합니다.

# Kafka Streams?

카프카 스트림즈는 Kafka 위에서 동작하는 클라이언트 라이브러리입니다.<br/>
실시간 데이터를 스트림 형태로 처리하고 지속적으로 필터링, 변환, 결합, 집계할 수 있게 해줍니다.<br/>
특히, 로컬 상태 저장소(RocksDB)를 활용하여 복잡한 데이터의 상태를 효율적으로 관리할 수 있고, 분산 처리와 고가용성을 내장하고 있어 복잡한 스트림 처리 애플리케이션을 쉽게 구축할 수 있도록 돕와줍니다.

카프카 스트림즈를 적용하기 전에 먼저 토폴로지에 대해 간략하게 살펴보려고 합니다.

> 카프카 스트림즈의 토폴로지는 데이터를 처리하는 흐름과 변환 과정을 정의하는 구조입니다.

주요 구성 요소
- **프로세서(Processor)**: 토폴로지를 구성하는 노드로, 데이터를 처리하는 역할을 합니다.
  - 소스 프로세서: 데이터를 카프카 토픽에서 읽어와 처리를 시작하는 최초의 노드입니다.
  - 스트림 프로세서: 이전 프로세서로부터 받은 데이터를 필터링, 변환, 조인, 집계하는 등 실제 데이터 처리 로직을 수행합니다.
  - 싱크 프로세서: 처리된 데이터를 특정 카프카 토픽으로 다시 저장하는 최종 노드입니다.
- **스트림(Stream)**: 프로세서 노드들을 연결하는 선이며, 프로듀서/컨슈머의 레코드와 동일한 토픽의 데이터를 의미합니다.

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-2.png?raw=true" width="60%">
</center>

## 정산 데이터 생성 단계

실제 정산 과정은 더 복잡하지만, 여기서는 이해를 돕기 위해 핵심 단계로 구성하여 카프카 스트림즈를 적용해 보려고 합니다.
- 결제 데이터 수신: 카프카를 통해 결제 데이터를 받아옵니다.
- 베이스(건별 내역) 생성: 수신된 결제 데이터를 바탕으로 건별 내역을 만듭니다.
- 비정산 결제건 필터링: 정산 대상이 아닌 결제건을 걸러냅니다.
- 정산 룰(지급 규칙) 조회: 적용할 지급 규칙을 찾아옵니다.
- 베이스(건별 내역) 저장: 처리된 개별 내역을 저장합니다.
- 건별 내역 집계: 개별 내역들을 모아 최종 정산 금액을 계산합니다.

# Kafka Streams 적용

카프카 스트림즈 애플리케이션을 만들기 위해 일반적으로 아래 패턴으로 진행됩니다.

1. StreamsConfig 인스턴스 생성
2. Serde 객체 생성
3. 처리 토폴로지 구성
4. 카프카 스트림즈 프로그램 시작

카프카 스트림즈 적용은 `Streams DSL` 또는 `processor API` 두 가지 방법이 있는데<br/>
본문에서는 Streams DSL(Domain Specific Language)을 활용하여 적용해 보려고 합니다.

두 방법의 간략한 차이는 아래와 같습니다.

|Streams DSL|processor API|
|---|---|
|일반적인 스트림 처리 작업을 위한 **고수준의 추상화**를 제공|스트림 처리 로직을 직접 정의하고 제어할 수 있는 **낮은 수준의 추상화**를 제공|
|필터링, 매핑, 집계, 조인 등과 같은 일반적인 **스트림 처리 작업을 간단하고 선언적인 방식으로** 수행|스트림 프로세서, 상태 저장소, 토폴로지 등을 **직접 정의하고 관리**|

Streams DSL 에서 제공하는 추상화된 메서드는 [Streams DSL Developer Guide](https://kafka.apache.org/30/documentation/streams/developer-guide/dsl-api.html)에서 확인할 수 있습니다.


## 1. StreamsConfig 인스턴스 생성

`StreamsConfig`에는 카프카 스트림즈 애플리케이션의 동작 방식을 정의하는 다양한 설정들이 들어갑니다.
- 애플리케이션의 기본 동작, Kafka 클러스터 연결, 데이터 직렬화/역직렬화, 상태 관리, 장애 처리, 성능 튜닝 등

```kotlin
// SettlementKafkaStreamsApp.kt
val streamsConfig = streamsConfig()

// KafkaStreamsConfig.kt
@Bean
fun streamsConfig(): StreamsConfig =
  StreamsConfig(Properties().apply {
    put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.paymentApplicationName)
    put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.servers)
    put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
    put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, serdeFactory.messagePaymentSerde().javaClass)
    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  })
```

여기서 사용할 카프카 스트림즈 애플리케이션의 설정을 살펴보겠습니다.

- `application.id`: 카프카 스트림즈 애플리케이션의 고유 식별자입니다. 
  - 카프카 클러스터 내에서 유일해야 하며, Kafka Consumer Group ID로 사용됩니다.
- `bootstrap.servers`: Kafka 브로커 서버의 주소 목록을 지정합니다. 
  - 초기 연결을 위해 사용되며, host:port 형태로 쉼표로 구분하여 여러 개 지정 가능합니다.
- `default.key.serde`: 카프카 토픽에서 메시지를 읽거나 쓸 때 키(Key)의 기본 직렬화/역직렬화(Serde) 방식을 지정합니다.
- `default.value.serde`: 카프카 토픽에서 메시지를 읽거나 쓸 때 값(Value)의 기본 직렬화/역직렬화(Serde) 방식을 지정합니다.
  - 메시지 키/값의 serde 객체는 기본값은 설정되어 있지 않으므로 명시적으로 설정해주어야 합니다.
  - 커스텀한 serde 객체를 사용할 수도 있습니다.
- `consumer.auto.offset.reset`: 카프카 컨슈머의 오프셋을 설정합니다.

## 2. 레코드 역직렬화를 위한 Serde 객체 생성

카프카에서 기본적으로 제공해주는 [Serdes](https://kafka.apache.org/21/javadoc/org/apache/kafka/common/serialization/Serdes.html#serdeFrom-java.lang.Class-) 객체를 사용하거나, 필요한 형태의 레코드를 사용하려면 커스텀한 객체 생성이 필요합니다.<br/>
여기서는 Json 형태의 `StreamMessage<Payment>` 객체로 메시지 값을 역직렬화화기 위해 커스텀한 Serde 객체를 생성해보겠습니다. 

```kotlin
// SerdeFactory.kt
fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
    // JsonDeserializer 생성
    val streamMessagePaymentDeserializer = JsonDeserializer(
        object : TypeReference<StreamMessage<Payment>>() {}, // 역직렬화 대상 타입 지정
        objectMapper, // JSON 처리를 위한 ObjectMapper
        false // failOnUnknownProperties flag
    )
    // 신뢰할 수 있는 패키지 설정
    streamMessagePaymentDeserializer.addTrustedPackages(
        "kafkastreams.study.sample.settlement.common.*",
        "kafkastreams.study.sample.settlement.domain.*",
    )

    // JsonSerde 객체 생성 및 반환
    return JsonSerde(
        JsonSerializer(objectMapper),
        streamMessagePaymentDeserializer
    )
}
```

- 역직렬화 대상 타입 지정
  - JsonDeserializer는 JSON 문자열을 어떤 객체로 변환해야 하는지 알아야 합니다.
- JSON 처리를 위한 ObjectMapper 
  - 날짜 형식, 특정 필드 무시, null 값 처리 등 다양한 JSON 처리 관련 설정이 적용된 objectMapper 인스턴스를 주입받아 일관된 방식으로 JSON을 처리합니다.
- failOnUnknownProperties 플래그
  - JsonDeserializer가 알 수 없는 JSON 속성(즉, 대상 객체에 매핑될 필드가 없는 속성)을 만났을 때 어떻게 동작할지를 결정합니다.
  - false로 설정하면, JSON 데이터에 StreamMessage<Payment> 객체에 정의되지 않은 속성이 있더라도 오류를 발생시키지 않고 해당 속성을 무시합니다.
  - 만약 true로 설정하면, 알 수 없는 속성이 있을 경우 역직렬화 과정에서 예외가 발생합니다. 
- 신뢰할 수 있는 패키지 설정
  - Jackson이 특정 상황(예: 다형성 처리 또는 특정 보안 설정 하에서)에서 역직렬화를 수행할 때, 아무 클래스나 역직렬화하지 않도록 제한하는 보안 기능과 관련이 있습니다.
  - addTrustedPackages() 메서드를 사용하여 역직렬화가 허용되는 패키지 경로를 명시적으로 지정합니다.
- JsonSerde 객체 생성 및 반환
  - JsonSerde는 카프카 스트림즈에서 사용할 수 있도록 직렬화기(Serializer)와 역직렬화기(Deserializer)를 하나로 묶은 클래스입니다.
  - 이렇게 생성된 `JsonSerde<StreamMessage<Payment>>` 객체는 카프카 스트림즈 토폴로지에서 `StreamMessage<Payment>` 타입의 데이터를 읽고 쓸 때 사용됩니다. 

## 3. 처리 토폴로지 구성

이제 만들게될 토폴로지의 구성을 살펴보겠습니다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-example.png?raw=true 'Result')

총 여섯 단계의 토폴로지를 한 단계씩 만들어 보겠습니다.

### 빌더 생성

`StreamsBuilder`는 토폴로지를 정의하기 위한 빌더 클래스입니다.

`StreamsBuilder`를 사용해서 여러 프로세서를 연결하여 데이터 처리 파이프라인을 구축할 수 있습니다.

```kotlin
val builder = StreamsBuilder()

// ...

@Bean
fun streamsConfig(): StreamsConfig =
  StreamsConfig(Properties().apply {
    put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.paymentApplicationName)
    put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.servers)
    put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
    put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, serdeFactory.messagePaymentSerde().javaClass)
    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  })
```
### 토픽으로부터 결제 데이터 받기

`Stream` 메서드는 토픽으로부터 소비한 메시지를 명시한 Serdes 객체 형태에 맞게 매핑하고 [KStream](https://kafka.apache.org/40/javadoc/org/apache/kafka/streams/kstream/KStream.html)을 생성합니다.

```kotlin
val paymentStream: KStream<String, StreamMessage<Payment>> = builder.stream(
  kafkaProperties.paymentTopic,
  Consumed.with(
    Serdes.String(),
    serdeFactory.messagePaymentSerde()
  )
)
```

디버깅/테스트 환경에서 print 메서드를 활용해서 단계별로 레코드의 상태를 확인할 수 있습니다.

```kotlin
// [payment-stream]: 5a54041d-2cce-43f5-8194-299acb8e8766, StreamMessage(channel=OFFLINE, action=PAYMENT, data=Payment(paymentType=OFFLINE, amount=65218, payoutDate=2025-05-21, confirmDate=2025-05-21, merchantNumber=merchant-1881, paymentDate=2025-05-19T21:48:15.989609, paymentActionType=PAYMENT, paymentMethodType=CARD))
paymentStream.print(Printed.toSysOut<String, StreamMessage<Payment>>().withLabel("payment-stream"))
```

### 결제 메시지 저장

토픽으로 수신한 결제 데이터를 로그성으로 저장하려고 한다면 [peek](https://kafka.apache.org/40/javadoc/org/apache/kafka/streams/kstream/KStream.html#peek(org.apache.kafka.streams.kstream.ForeachAction)) 메서드를 활용할 수 있습니다.

`peek` 메서드는 각 레코드에 대해 작업을 수행하고 변경되지 않은 스트림을 반환합니다.

```kotlin
paymentStream
    .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
```

### 결제 데이터로 정산 베이스 생성

레코드의 값을 새로운 형태로 매핑하기 위해서 [mapValues](https://kafka.apache.org/40/javadoc/org/apache/kafka/streams/kstream/KStream.html#mapValues(org.apache.kafka.streams.kstream.ValueMapper)) 메서드를 활용할 수 있습니다.

각 입력 레코드의 키값은 유지하면서 새로운 값으로 변환합니다.

```kotlin
paymentStream
    .mapValues(BaseMapper())
```

Mapper 구현
- `ValueMapper` 인터페이스를 구현하고, 입력으로 `value type`, 출력으로 `mapped value type` 을 명시합니다.
- 여기서는 `StreamMessage<Payment>` 타입을 `Base` 타입으로 매핑하였습니다.

```kotlin
class BaseMapper() : ValueMapper<StreamMessage<Payment>, Base> {
  override fun apply(payment: StreamMessage<Payment>): Base {
    return Base(
      paymentType = payment.data?.paymentType,
      amount = payment.data.amount,
      payoutDate = payment.data.payoutDate,
      confirmDate = payment.data.confirmDate,
      merchantNumber = payment.data.merchantNumber,
      paymentDate = payment.data.paymentDate,
      paymentActionType = payment.data.paymentActionType,
      paymentMethodType = payment.data.paymentMethodType,
    )
  }
}
```

### 비정산 또는 중복 결제건 필터링

결제 데이터 중에서도 비정산(테스트 결제, 비정산 가맹점, 망취소, 미확인 등)또는 중복 결제건에 해당하는 데이터는 UnSettlement로 분류하고, 정산 대상의 데이터만 파이프라인을 이어갈 수 있도록 [filter](https://kafka.apache.org/40/javadoc/org/apache/kafka/streams/kstream/KStream.html#filter(org.apache.kafka.streams.kstream.Predicate)) 메서드를 사용할 수 있습니다.

`filter` 메서드는 주어진 조건을 만족하는 레코드의 KStream 을 반환하고, 조건을 만족하지 않는 레코드는 삭제됩니다.

```kotlin
paymentStream
        .filter { _, base -> settlementService.isSettlement(base) }
```

### 지급룰 조회 및 세팅

`상태 저장소`를 연결해서 레코드를 하나씩 처리하기 위해 [processValues](https://kafka.apache.org/40/javadoc/org/apache/kafka/streams/kstream/KStream.html#processValues(org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier,java.lang.String...)) 메서드를 활용할 수 있습니다.
- 상태 저장소를 연결하기 위해 `FixedKeyProcessorSupplier` 에서 제공하는 `FixedKeyProcessor`를 적용해야 합니다.

여기서 `상태 저장소`를 간략하게 살펴보면<br/> 
`RocksDB`와 같은 로컬 저장소를 활용하여 `KTable`로 키-값 데이터를 관리하고, `변경 로그 토픽`을 통해 상태를 복원하여 내결함성을 제공하며, `윈도우 기반 처리`로 특정 기간 내 데이터 집계 및 분석이 가능합니다.

상태 저장소의 한 가지 단점이 있다면, 데이터가 파티션마다 분산되어 저장되므로 조회 시 파티션 전체로 조회가 필요합니다.<br/>
그렇지 않을 경우 파티션 별로 데이터가 달라질 수 있습니다.<br/>
이 단점은 `Interactive Queries`를 활용하여, 특정 key를 담당하는 파티션의 인스턴스의 호스트 정보를 알아내고, 만약 key가 다른 인스턴스에 있다면, 해당 인스턴스의 HTTP 엔드포인트로 요청을 보내 데이터를 가져올 수 있습니다.

대안으로 `GlobalKTable`을 사용할 수 있는데<br/>
별도의 토픽으로 데이터를 관리하고, 이 토픽을 소스로 하는 GlobalKTable을 생성합니다.<br/>
GlobalKTable은 해당 토픽의 모든 데이터를 각 Kafka Streams 인스턴스에 복제합니다.<br/>
따라서 각 인스턴스는 전체 Rule 데이터의 로컬 복사본을 가지게 되어, 어떤 key에 대해서도 로컬에서 빠르게 조회할 수 있습니다.<br/>
이 방법은 "글로벌 캐시"와 유사하게 동작하며, 모든 인스턴스가 전체 데이터셋에 접근해야 할 때 매우 유용합니다<br/>

참고. 단순하게 레디스를 활용할 수 있지만 상태 저장소의 활용을 위해 적용해 보겠습니다.

```kotlin
builder.globalTable(
  kafkaProperties.paymentRulesGlobalTopic,
  Materialized.`as`<String, Rule, KeyValueStore<Bytes, ByteArray>>(GLOBAL_PAYOUT_RULE_STATE_STORE_NAME)
    .withKeySerde(Serdes.String())
    .withValueSerde(serdeFactory.ruleSerde())
)

// ...

paymentStream.processValues(
  PayoutRuleProcessValues(
    rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
    stateStoreName = GLOBAL_PAYOUT_RULE_STATE_STORE_NAME,
    payoutRuleClient = payoutRuleClient,
    ruleKafkaTemplate = ruleKafkaTemplate,
  ),
)
```

`FixedKeyProcessorSupplier`, `FixedKeyProcessor` 구현

```kotlin
class PayoutRuleProcessValues(
  private val rulesGlobalTopic: String,
  private val stateStoreName: String,
  private val payoutRuleClient: PayoutRuleClient,
  private val ruleKafkaTemplate: KafkaTemplate<String, Rule>,
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
  private var payoutRuleStore: ReadOnlyKeyValueStore<String, Rule>? = null

  override fun init(context: FixedKeyProcessorContext<String, Base>) {
    this.context = context
    this.payoutRuleStore = this.context?.getStateStore(stateStoreName)
  }

  override fun process(record: FixedKeyRecord<String, Base>) {
    val key = record.key()
    val base = record.value()

    // 결제 데이터가 없을 경우 스킵
    if (base == null) {
      log.info(">>> [결제 데이터 누락] Payment data is null, skipping processing for key: $key")
      return
    }

    // stateStore에 저장된 지급룰 조회
    val ruleKey = "${base.merchantNumber}/${base.paymentDate.toLocalDate()}/${base.paymentActionType}/${base.paymentMethodType}"
    var rule = payoutRuleStore?.get(ruleKey)
    // stateStore에 지급룰이 저장되어 있지 않을 경우 API 요청 후 저장
    if (rule == null) {
      log.info(">>> [지급룰 조회] Search payout rule.. $ruleKey")
      val findRule = payoutRuleClient.getPayoutDate(
        PayoutDateRequest(
          merchantNumber = base.merchantNumber ?: throw IllegalArgumentException(),
          paymentDate = base.paymentDate,
          paymentActionType = base.paymentActionType ?: throw IllegalArgumentException(),
          paymentMethodType = base.paymentMethodType ?: throw IllegalArgumentException(),
        )
      )
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
      log.info(">>> [지급룰 저장] Save payout date.. $ruleKey")
      base.updatePayoutDate(rule)
    }

    context?.forward(record.withValue(base))
  }

  override fun close() {
    this.close()
  }

  companion object {
    private val log by logger()
  }
}
```

GlobalKTable 전용 토픽(payout-rules-global-topic)
- key: merchant-8694/2025-05-25/CANCEL/MONEY 
- value:

  ```json
  {
    "ruleId": "7a88c5b1-0202-486c-be0c-239f7776f857",
    "payoutDate": "2025-05-29",
    "confirmDate": "2025-05-28",
    "merchantNumber": "merchant-8694",
    "paymentDate": "2025-05-25T18:08:00.890907",
    "paymentActionType": "CANCEL",
    "paymentMethodType": "MONEY"
  }
  ```


TODO: GlobalKTable 어떻게 저장되는지,
- stateStore 어떤 구조로 저장되는지

### 정산 베이스 저장

결제 메시지 저장과 동일하게 `peek` 메서드를 사용하서 각 레코드를 저장합니다.

```kotlin
paymentStream
    .peek({ _, message -> settlementService.saveBase(message) })
```

### 집계

```kotlin
baseStream.groupBy(
  { _, base -> base.toAggregationKey() },
  Grouped.with( // 그룹화에 사용될 복합 키, 원본 Base 를 위한 Serdes 지정
    serdeFactory.baseAggregationKeySerde(),
    serdeFactory.baseSerde()
  )
)
  .aggregate( // 그룹별로 집계 수행
    { BaseAggregateValue() }, // 각 그룹의 집계가 시작될 때 초기값을 반환
    // (그룹 키, 새로운 값, 현재 집계값) -> 새로운 집계값
    { _aggKey, newBaseValue, currentAggregate ->
      currentAggregate.updateWith(newBaseValue.amount)
    },
    // 집계 결과를 저장할 상태 저장소 및 Serdes 설정
    Materialized.`as`<BaseAggregationKey, BaseAggregateValue, KeyValueStore<Bytes, ByteArray>>(
      STATISTICS_STORE_NAME
    )
      .withKeySerde(serdeFactory.baseAggregationKeySerde())   // KTable의 키(BaseAggregationKey) Serde
      .withValueSerde(serdeFactory.baseAggregateValueSerde()) // KTable의 값(BaseAggregateValue) Serde
  )
```

집계 조회

```json
{
    "key": {
        "merchantNumber": "merchant-4436",
        "paymentActionType": "PAYMENT",
        "paymentDateDaily": "2025-05-26",
        "paymentMethodType": "MONEY"
    },
    "value": {
        "count": 5,
        "totalAmount": 3540674
    }
},
{
    "key": {
        "merchantNumber": "merchant-6076",
        "paymentActionType": "PAYMENT",
        "paymentDateDaily": "2025-05-26",
        "paymentMethodType": "CARD"
    },
    "value": {
        "count": 2,
        "totalAmount": 1550510
    }
},
```

## 카프카 스트림즈 인스턴스 생성

```kotlin
KafkaStreams(builder.build(), streamsConfig)
```

## 전체 코드

.. 메서드를 활용하여 스트림 파이프라인을 구성해 보았는데 그밖에도 카프카 스트림즈 
https://kafka.apache.org/30/documentation/streams/developer-guide/dsl-api.html#id10

