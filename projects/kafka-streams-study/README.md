# 스트림하게 정산 데이터 생성해보기

정산플랫폼팀에서 결제 데이터를 기반으로 정산에 필요한 데이터를 생성하는 역할을 맡고 있습니다.

결제팀으로부터 매일 수백만 건 이상의 결제 데이터가 카프카(`kafka`)를 통해 전달되고 있는데, 최근 업무에 파이프라인 방식으로 데이터를 처리하기 위해 카프카 스트림즈(`kafka streams`)를 적용해 보며,<br/>
'기존에 비실시간으로 생성되던 정산 데이터를 카프카 스트림즈를 통해 실시간 스트림으로 생성하면 어떨까?' 하는 생각을 하게 되었습니다.

기존 카프카 인프라에서 별도의 클러스터 없이도 손쉽게 스트림 처리 애플리케이션을 구축할 수 있게 해주는 카프카 스트림즈의 매력을<br/>
간단히 축약한 정산 데이터 생성 샘플에 적용해 보면서 공유해 드리고자 합니다.

# Kafka Streams?

카프카 스트림즈는 카프카 위에서 동작하는 클라이언트 라이브러리입니다. 실시간 데이터를 스트림 형태로 처리하고 연속적으로 필터링(filtering), 변환(transform), 결합(joining), 집계(aggregating)할 수 있게 도와줍니다.<br/>
특히, 로컬 상태 저장소(RocksDB)를 활용하여 복잡한 데이터의 상태를 효율적으로 관리할 수 있고, 분산 처리와 고가용성을 내장하고 있어 복잡한 스트림 처리 애플리케이션을 쉽게 구축할 수 있도록 돕와줍니다.
- 분산 처리: 여러 인스턴스가 작업을 나누어 병렬로 처리하여 성능과 처리량을 향상
- 고가용성: 일부 인스턴스에 장애가 발생해도 다른 인스턴스가 작업을 이어받고 상태도 안전하게 복원되어 서비스 중단을 최소화

본격적으로 카프카 스트림즈를 적용해보기 전에 먼저 가장 기본적이면서 핵심 개념인 `토폴로지`에 대해 간략하게 살펴보려고 합니다.

> 카프카 스트림즈의 토폴로지는 **데이터를 처리하는 흐름과 변환 과정을 정의하는 구조**입니다.

토폴로지의 주요 구성 요소로는 두 가지가 존재합니다.
- **프로세서(Processor)**: 토폴로지를 구성하는 노드로, **데이터를 처리하는 역할**을 합니다.
  - **소스 프로세서**: 데이터를 카프카 토픽에서 읽어와 처리를 시작하는 최초의 노드입니다.
  - **스트림 프로세서**: 이전 프로세서로부터 받은 데이터를 필터링, 변환, 결합, 집계 등 실제 데이터 처리 로직을 수행합니다.
  - **싱크 프로세서**: 처리된 데이터를 특정 카프카 토픽으로 다시 저장하는 최종 노드입니다.
- **스트림(Stream)**: 프로세서 노드들을 연결하는 선이며, 프로듀서/컨슈머의 레코드와 동일한 **토픽의 데이터를 의미**합니다.

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-2.png?raw=true" width="60%">
</center>

## 정산 데이터 생성 단계

실제 정산 데이터 생성 과정은 복잡하지만, 이해를 돕기 위해 카프카 스트림즈 적용 단계를 간단히 축약했습니다.

1. 결제 데이터 수신: 카프카를 통해 결제 데이터를 받아옵니다.
2. 결제 메시지 저장: 결제 메시지를 로그로 저장합니다.
3. 베이스(건별 내역) 생성: 수신된 결제 데이터를 바탕으로 건별 내역을 생성합니다.
4. 비정산/중복 결제건 필터링: 정산 대상이 아닌 결제건을 걸러냅니다.
5. 지급룰 조회: 정산에 필요한 지급 규칙을 조회합니다.
6. 베이스(건별 내역) 저장: 처리된 건별 내역을 저장합니다.
7. 건별 내역 집계: 건별 내역들을 집계하여 정산 금액을 계산합니다.
8. 집계 결과 전송: 일정산 집계 결과를 토픽으로 전송합니다.

# Kafka Streams 적용

카프카 스트림즈 애플리케이션은 일반적으로 다음 단계를 거쳐 만듭니다.

1. StreamsConfig 인스턴스 생성
2. Serde 객체 생성
3. 처리 토폴로지 구성
4. 카프카 스트림즈 프로그램 시작

카프카 스트림즈를 적용하는 방법에는 `Streams DSL`과 `processor API` 두 가지 방법가 있는데, 이 글에서는 Streams DSL(Domain Specific Language)을 활용해 보려고 합니다.

두 방법의 간략한 차이는 다음과 같습니다.

| Streams DSL                                                   |processor API|
|---------------------------------------------------------------|---|
| 일반적인 스트림 처리 작업을 위한 **고수준의 추상화**를 제공                           |스트림 처리 로직을 직접 정의하고 제어할 수 있는 **낮은 수준의 추상화**를 제공|
| 필터링, 변환, 결합, 집계 등과 같은 **일반적인 스트림 처리 작업을 간단하고 선언적인 방식으로** 수행 |스트림 프로세서, 상태 저장소, 토폴로지 등을 **직접 정의하고 관리**|

Streams DSL 에서 제공하는 추상화된 모든 메서드는 [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html#)에서 확인할 수 있습니다.

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

카프카에서 기본적으로 제공해주는 [Available Serdes](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html#available-serdes)를 사용하거나, 필요한 형태의 레코드를 사용하려면 커스텀한 객체 생성이 필요합니다.<br/>
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

- **`JsonDeserializer` 생성**
  - 역직렬화 대상 타입 지정
    - `JsonDeserializer`는 JSON 문자열을 어떤 객체로 변환해야 하는지 알아야 하므로 역직렬화 대상 타입을 지정해야 합니다.
  - JSON 처리를 위한 `ObjectMapper` 
    - 날짜 형식, 특정 필드 무시, null 값 처리 등 다양한 JSON 처리 관련 설정이 적용된 `objectMapper` 인스턴스를 주입받아 일관된 방식으로 JSON을 처리할 수 있습니다.
  - `failOnUnknownProperties` 플래그
    - JsonDeserializer가 알 수 없는 JSON 속성(ex. 대상 객체에 매핑될 필드가 없는 속성)을 만났을 때 어떻게 동작할지를 결정합니다.
    - false 설정: JSON 데이터에 역직렬화 대상 타입 객체에 정의되지 않은 속성이 있더라도 오류를 발생시키지 않고 해당 속성을 무시합니다.
    - true 설정: 역직렬화 대상 타입 객체에 알 수 없는 속성이 있을 경우 역직렬화 과정에서 예외가 발생합니다. 
- **신뢰할 수 있는 패키지 설정**
  - Jackson이 역직렬화를 수행할 때, 아무 클래스나 역직렬화하지 않도록 제한하는 기능입니다.
  - addTrustedPackages() 메서드를 사용하여 역직렬화가 허용되는 패키지 경로를 명시적으로 지정합니다.
- **`JsonSerde` 객체 생성 및 반환**
  - `JsonSerde`는 카프카 스트림즈에서 사용할 수 있도록 직렬화기(Serializer)와 역직렬화기(Deserializer)를 하나로 묶은 클래스입니다.
  - 이렇게 생성된 `JsonSerde<ClassA>` 객체는 카프카 스트림즈 토폴로지에서 `ClassA` 타입의 데이터를 읽고 쓸 때 사용됩니다. 

📚 [Kafka Streams Data Types and Serialization for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html#kstreams-data-types-and-serialization-for-cp)

## 3. 처리 토폴로지 구성

카프카 스트림즈 적용을 위한 기본적인 준비는 되었습니다. 이제 생성하게 될 토폴로지의 구성을 살펴보겠습니다.

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-example.png?raw=true" width="60%">
</center>

토폴로지를 정의하기 위해 먼저 `StreamsBuilder`라는 빌더 생성이 필요합니다. 
- `StreamsBuilder`를 사용해서 여러 프로세서를 연결하고, 데이터 처리 파이프라인을 구축할 수 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
val builder = StreamsBuilder()
```

이제 정산 데이터 생성을 위해 여섯 단계의 토폴로지를 한 개씩 만들어 보겠습니다. 

### 1단계. 토픽으로부터 결제 데이터 받기

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/source-processor.png?raw=true" width="50%">
</center>

소스 스트림에 해당하는 `stream` 메서드(input topics → KStream)는 토픽으로부터 소비한 메시지를 명시한 Serdes 객체 형태에 맞게 매핑하고 레코드 스트림 [KStream](https://docs.confluent.io/platform/current/streams/concepts.html#kstream)을 생성합니다.
- `Serdes`를 명시적으로 지정하지 않으면 streamsConfig 구성의 기본 Serdes가 사용되고, Kafka 입력 토픽에 있는 레코드의 키 또는 값 유형이 구성된 기본 Serdes와 일치하지 않는 경우 Serdes를 명시적으로 지정해야 합니다.

```kotlin
val paymentStream: KStream<String, StreamMessage<Payment>> = builder.stream(
  kafkaProperties.paymentTopic,
  Consumed.with(
    Serdes.String(),
    serdeFactory.messagePaymentSerde()
  )
)
```

디버깅/테스트 환경에서 print 메서드를 활용해서 단계별로 레코드의 상태를 확인할 수도 있습니다.

```kotlin
// [payment-stream]: 5a54041d-2cce-43f5-8194-299acb8e8766, StreamMessage(channel=OFFLINE, action=PAYMENT, data=Payment(paymentType=OFFLINE, amount=65218, payoutDate=2025-05-21, confirmDate=2025-05-21, merchantNumber=merchant-1881, paymentDate=2025-05-19T21:48:15.989609, paymentActionType=PAYMENT, paymentMethodType=CARD))
paymentStream.print(Printed.toSysOut<String, StreamMessage<Payment>>().withLabel("payment-stream"))
```

### 2단계. 결제 메시지 저장

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-peek.png?raw=true" width="80%">
</center>

`stream` 메서드를 통해 수신한 결제 데이터를 [peek](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#peek-org.apache.kafka.streams.kstream.ForeachAction-) 연산에 적용된 람다 함수를 통해 로그에 저장합니다.

`peek` 메서드는 각 레코드에 대해 작업을 수행하고 변경되지 않은 스트림을 반환합니다.
- peek는 로깅이나 메트릭 추적, 디버깅 및 트러블슈팅과 같은 상황에 유용하게 사용할 수 있습니다.
- 스트림 데이터에 대한 수정 작업이 필요할 경우 `map`, `mapValues` 같은 메서드를 사용할 수 있습니다.

```kotlin
paymentStream
    .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
```

### 3단계. 결제 데이터로 정산 베이스 생성

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-mapValue.png?raw=true" width="100%">
</center>

[mapValues](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#map-org.apache.kafka.streams.kstream.KeyValueMapper-) 메서드를 통해 기존 스트림의 메시지 키는 유지하면서 값을 기존 타입(`StreamMessage<Payment>`)에서 새로운 타입(`Base`)으로 변환합니다.

```kotlin
paymentStream
    .mapValues(BaseMapper())
```

mapValues 연산자에 전달하기 위한 `ValueMapper` 구현체를 정의합니다.
- `ValueMapper<V, VR>` 인터페이스는 입력 값 타입 `V`를 출력 값 타입 `VR`로 변환하는 역할을 합니다.
- 여기서 `V`는 `StreamMessage<Payment>`이고, `VR`은 `Base`입니다.
- 기존 스트림의 각 `StreamMessage<Payment>` 값을 `Base` 객체로 어떻게 변환할지에 대한 구체적인 로직을 정의합니다.

```kotlin
class BaseMapper() : ValueMapper<StreamMessage<Payment>, Base> {
  // 스트림의 각 메시지에 대해 apply 메서드를 호출하며, 메시지의 값을 인자로 전달
  override fun apply(payment: StreamMessage<Payment>): Base {
    return Base(
      paymentType = payment.data?.paymentType ?: throw IllegalArgumentException(),
      amount = payment.data.amount,
      payoutDate = payment.data.payoutDate,
      confirmDate = payment.data.confirmDate,
      merchantNumber = payment.data.merchantNumber ?: throw IllegalArgumentException(),
      paymentDate = payment.data.paymentDate,
      paymentActionType = payment.data.paymentActionType ?: throw IllegalArgumentException(),
      paymentMethodType = payment.data.paymentMethodType ?: throw IllegalArgumentException(),
    )
  }
}

```

### 4단계. 비정산 또는 중복 결제건 필터링

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-filter.png?raw=true" width="100%">
</center>

베이스가 생성되고 결제 데이터 중에서도 비정산(테스트 결제, 비정산 가맹점, 망취소, 미확인 등) 또는 중복 결제건에 해당하는 데이터는 UnSettlement, Duplicated로 분류하고,<br/>
정산 대상의 데이터만 다음 파이프라인을 이어갈 수 있도록 [filter](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#filter-org.apache.kafka.streams.kstream.Predicate-) 메서드를 사용합니다.
- 비정산과 중복 대상은 별도의 filter 프로세서로 처리하는 것이 명확하지만, 설명의 간소화를 위해 합치게 되었습니다.  

`filter` 메서드는 주어진 조건을 만족하는 레코드의 KStream을 반환하고, 조건을 만족하지 않는 레코드는 삭제됩니다.

```kotlin
paymentStream
    .filter { _, base -> settlementService.isSettlement(base) }
```

### 5단계. 지급룰 조회 및 세팅

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-processValues.png?raw=true" width="100%">
</center>

정산 대상의 데이터에 지급룰 정보를 세팅하려고 합니다.<br/>
지급룰은 API 호출을 통해 제공받고 있는데, 중복되는 지급룰은 따로 저장해서 API 호출로 인한 네트워크 통신 비용을 절약하고자 합니다.

이 상황에서 단순하게 레디스를 활용할 수도 있지만 카프카 스트림즈의 `상태 저장소`를 사용해 보려고 합니다.<br/>
상태 저장소는 `RocksDB`와 같은 로컬 저장소를 활용하여 `KTable`로 키-값 데이터를 관리하고, `변경 로그 토픽`을 통해 상태를 복원하여 내결함성을 제공하며, `윈도우 기반 처리`로 특정 기간 내 데이터 집계 및 분석이 가능합니다.

`상태 저장소`를 연결해서 레코드를 하나씩 처리하기 위해 [processValues()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#processValues-org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier-java.lang.String...-) 메서드를 사용합니다.<br/>
`.processValues()` 메서드는 스트림의 각 레코드에 대해 키는 변경되지 않고, 값만을 대상으로 사용자 정의 로직을 실행하고자 할 때 사용할 수 있습니다.<br/>
`FixedKeyProcessorSupplier` 인터페이스를 구현한 객체를 인자로 전달하기 위해 구현체가 필요합니다.

```kotlin
// SettlementKafkaStreamsApp.kt
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
    .processValues( // 사용자 정의 상태 기반 값 처리 로직을 적용
      PayoutRuleProcessValues(
        rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
        stateStoreName = kafkaProperties.globalPayoutRuleStateStoreName,
        payoutRuleClient = payoutRuleClient,
        ruleKafkaTemplate = ruleKafkaTemplate,
      ),
    )
```

`FixedKeyProcessorSupplier` 인터페이스 구현체

```kotlin
class PayoutRuleProcessValues(
  private val rulesGlobalTopic: String, // GlobalKTable의 소스 토픽 이름
  private val stateStoreName: String, // GlobalKTable의 로컬 상태 저장소 이름
  private val payoutRuleClient: PayoutRuleClient, // 외부 API 호출을 위한 클라이언트
  private val ruleKafkaTemplate: KafkaTemplate<String, Rule>, // 지급룰 정보를 토픽으로 보내기 위한 템플릿
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

    // 지급 규칙 조회를 위한 키 생성 (가맹점번호/결제일/결제액션타입/결제수단타입)
    val ruleKey = "${base.merchantNumber}/${base.paymentDate.toLocalDate()}/${base.paymentActionType}/${base.paymentMethodType}"
    // 상태 저장소(GlobalKTable)에서 ruleKey로 지급 규칙 조회
    val valueAndTimestamp = payoutRuleStore?.get(ruleKey)
    var rule = valueAndTimestamp?.value()
    // 상태 저장소에 지급 규칙이 없을 경우
    if (rule == null) {
      log.info(">>> [지급룰 조회] Search payout rule.. $ruleKey")
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

상태 저장소에 저장되는 데이터는 `변경 로그 토픽`을 통해 상태를 복원할 수 있다고 했었는데요.<br/>
토폴로지에 GlobalKTable을 정의할 때 GlobalKTable이 데이터를 읽어올 토픽 이름을 지정했었는데 바로 해당 토픽을 보면 지급룰 정보가 저장되어 있는 것을 확인할 수 있습니다.

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

💡**상태 저장소를 사용하는 방식**

카프카 스트림즈에서 상태를 관리하고 상태 저장소를 활용하는 주요 방법으로 `KTable`과 `GlobalKTable`이 있습니다.

두 방식에 대한 차이를 잠시 보고 넘어가보려고 합니다.

|           | KTable                                                                                                                                                                                        | GlobalKTable                                                                |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| 데이터 저장 방식 | 데이터 분산 저장                                                                                                                                                                                     | 모든 인스턴스의 상태 저장소에 데이터 복제 |
| 조회        | 조회 대상 키가 어떤 파티션에 있는지 알아야 하며, 데이터가 다른 인스턴스에 있다면 네트워크를 통한 조회 필요 | 각 인스턴스가 전체 데이터의 로컬 복사본을 가지고 있으므로, 어떤 키에 대해서도 파티션에 관계없이 로컬에서 빠르게 조회          |
| 변경 로그     | 상태 변경 사항을 내부적으로 변경 로그 토픽에 기록                                                                                                                                                                  | 소스 토픽 자체가 변경 로그의 역할(이 소스 토픽의 데이터를 모든 인스턴스가 소비하여 로컬 상태를 최신으로 유지)             |

지급룰을 상태 저장소에 저장할 때 `GlobalKTable`을 활용한 것을 보셨을 것입니다.<br/>
`GlobalKTable`은 `KTable`과 다르게 모든 인스턴스가 소스 토픽을 변경 로그의 원천으로 사용하므로, 각 인스턴스는 소스 토픽에서 읽어온 데이터를 사용하여 자신의 로컬 상태 저장소를 업데이트합니다.<br/>
이렇게 함으로써 모든 인스턴스가 전체 데이터셋의 완전한 복제본을 로컬에 유지하게 되어, 파티션에 관계없이 로컬에서 빠르게 조회가 가능합니다.

반면, `KTable`은 소스 토픽의 파티션과 1:1로 매핑되는 별도의 변경 로그 토픽(이 토픽의 이름은 일반적으로 애플리케이션ID-상태저장소이름-changelog 형태의 패턴을 따름)을 생성하고 사용합니다.</br>
따라서 각 인스턴스는 자신이 담당하는 파티션의 변경 로그만 소비하여 로컬 상태를 관리하다보니, 데이터가 파티션마다 분산되어 저장되어 조회 시 파티션 전체로 조회가 필요한 단점이 있습니다.<br/>
이 단점은 `Interactive Queries`를 활용하여, 특정 key를 담당하는 파티션의 인스턴스의 호스트 정보를 알아내고, 만약 key가 다른 인스턴스에 있다면, 해당 인스턴스의 HTTP 엔드포인트로 요청을 보내 데이터를 가져올 수 있지만,<br/>
지급룰 조회에 불필요한 네트워크 통신이 필요하게 될 수 있어 캐시처럼 활용하기 위해 `GlobalKTable`을 활용하게 되었습니다.

### 6단계. 정산 베이스 저장

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-peek-2.png?raw=true" width="80%">
</center>

결제 메시지 저장에서 사용했던 [peek](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#peek-org.apache.kafka.streams.kstream.ForeachAction-)를 활용해서 정산 베이스를 데이터베이스에 저장합니다.

```kotlin
paymentStream
  .peek({ _, message -> settlementService.saveBase(message) })
```

### 7단계. 집계

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-aggregate.png?raw=true" width="100%">
</center>

정산 베이스 통계를 만들기 위해 스트림 레코드를 집계하려고 합니다.<br/>
집계하기 전에 [groupBy](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KTable.html#groupBy-org.apache.kafka.streams.kstream.KeyValueMapper-)를 활용하여 스트림 레코드의 키와 값을 적절하게 지정합니다.<br/>
결과로 생성된 `KGroupedStream`에 [aggregate](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KGroupedStream.html#aggregate-org.apache.kafka.streams.kstream.Initializer-org.apache.kafka.streams.kstream.Aggregator-org.apache.kafka.streams.kstream.Materialized-)를 적용하여 그룹화된 키를 기준으로 레코드의 값을 집계합니다.

집계 결과는 이전 지급룰 조회 및 세팅 단계에서 언급된 `KTable` 형태로 저장되므로 조회를 위해 `Interactive Queries`를 활용할 수 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
val aggregatedTable: KTable<BaseAggregationKey, BaseAggregateValue> = baseStream.groupBy(
  { _, base -> base.toAggregationKey() }, // 집계에 사용될 키
  Grouped.with( // 그룹화 연산을 수행할 때 키와 값을 어떻게 직렬화/역직렬화할지 명시
    serdeFactory.baseAggregationKeySerde(),
    serdeFactory.baseSerde()
  )
)
  .aggregate( // groupBy를 통해 생성된 KGroupedStream에 대해 각 그룹별로 집계 연산을 수행
    { BaseAggregateValue() }, // 신규 그룹 키가 생성될 때, 해당 그룹의 집계를 시작하기 위한 초기값
    { _aggKey, newBaseValue, currentAggregate -> // 각 그룹에 새로운 레코드가 도착할 때마다 호출
      currentAggregate.updateWith(newBaseValue.amount) // (그룹 키, 새로운 값, 현재 집계값) -> 새로운 집계값
    },
    // 집계 연산 결과를 상태 저장소에 저장하기 위한 설정
    Materialized.`as`<BaseAggregationKey, BaseAggregateValue, KeyValueStore<Bytes, ByteArray>>(
      kafkaProperties.statisticsStoreName
    )
      .withKeySerde(serdeFactory.baseAggregationKeySerde())
      .withValueSerde(serdeFactory.baseAggregateValueSerde())
  )

// 집계에 사용될 키 정의 (Base.kt)
fun toAggregationKey() = 
  BaseAggregationKey(
    merchantNumber = this.merchantNumber,
    paymentDateDaily = this.paymentDate.toLocalDate(),
    paymentActionType = this.paymentActionType,
    paymentMethodType = this.paymentMethodType
  )

// 집계를 시작하기 위한 초기값 및 집계 계산식 정의 (BaseAggregateValue.kt)
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

<center>
  <img src="https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-to.png?raw=true" width="100%">
</center>

KTable 형태의 집계 결과를 [toStream](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KTable.html#toStream--)을 적용해 KStream으로 변환한 후, [to](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#to-java.lang.String-org.apache.kafka.streams.kstream.Produced-)메서드를 통해 다른 토픽으로 전송합니다.<br/>
`Interactive Queries`를 활용한 조회로 집계 단계를 마무리할 수 있지만, 토픽으로 결과를 전송하면서 히스토리를 남기거나 집계 결과를 검증하는 데 사용할 수 있습니다.

```kotlin
aggregatedTable.toStream()
    .to(
        kafkaProperties.paymentStatisticsTopic,
        Produced.with(
            serdeFactory.baseAggregationKeySerde(),
            serdeFactory.baseAggregateValueSerde()
        )
    )
```

## 4. 카프카 스트림즈 인스턴스 생성

KafkaStreams 인스턴스의 [start()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/KafkaStreams.html#start--) 메서드를 호출하면 인스턴스를 시작할 수 있습니다.

예제에서는 KafkaStreams를 Bean으로 등록하고 별도의 Runner를 통해 실행하였습니다.

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

## 전체 코드

지금까지의 과정을 연결시켜보면 집계 부분이 다소 코드가 길어보일 수 있지만, 카프카 스트림즈를 통해 간결한 코드로 파이프라인을 형성할 수 있게 되었습니다.

```kotlin
@Bean
fun settlementStreams(): KafkaStreams {
    val streamsConfig = streamsConfig()
    val builder = StreamsBuilder()
    applyGlobalTable(builder)

    val paymentStream: KStream<String, StreamMessage<Payment>> = builder.stream(
        kafkaProperties.paymentTopic,
        Consumed.with(
            Serdes.String(),
            serdeFactory.messagePaymentSerde()
        )
    )

    val baseStream = paymentStream
        .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
        .mapValues(BaseMapper())
        .filter { _, base -> settlementService.isSettlement(base) }
        .processValues(
            PayoutRuleProcessValues(
                rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
                stateStoreName = kafkaProperties.globalPayoutRuleStateStoreName,
                payoutRuleClient = payoutRuleClient,
                ruleKafkaTemplate = ruleKafkaTemplate,
            ),
        )
        .peek({ _, message -> settlementService.saveBase(message) })

    val aggregatedTable: KTable<BaseAggregationKey, BaseAggregateValue> = baseStream.groupBy(
        { _, base -> base.toAggregationKey() },
        Grouped.with(
            serdeFactory.baseAggregationKeySerde(),
            serdeFactory.baseSerde()
        )
    )
        .aggregate(
            { BaseAggregateValue() },
            { _aggKey, newBaseValue, currentAggregate ->
                currentAggregate.updateWith(newBaseValue.amount)
            },
            Materialized.`as`<BaseAggregationKey, BaseAggregateValue, KeyValueStore<Bytes, ByteArray>>(
                kafkaProperties.statisticsStoreName
            )
                .withKeySerde(serdeFactory.baseAggregationKeySerde())
                .withValueSerde(serdeFactory.baseAggregateValueSerde())
        )

    aggregatedTable.toStream()
        .to(
            kafkaProperties.paymentStatisticsTopic,
            Produced.with(
                serdeFactory.baseAggregationKeySerde(),
                serdeFactory.baseAggregateValueSerde()
            )
        )

    return KafkaStreams(builder.build(), streamsConfig)
}
```

## 마치며

기존 비실시간으로 처리되던 처리를 실시간으로 스트림하게 처리되도록 적용해 보면서 카프카 스트림즈에 대한 매력을 맛볼 수 있었습니다.<br/>
카프카를 사용하여 복잡한 로직을 처리중이시다면 카프카 스트림즈를 활용하여 간편하게 스트림 처리 애플리케이션을 구축해보는게 어떨까요?

## 참고
- [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html)
- [Kafka Streams Architecture for Confluent Platform](https://docs.confluent.io/platform/current/streams/architecture.html)
- [Kafka Streams Interactive Queries for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/interactive-queries.html)

