# 정산 데이터, 이제 스트리밍으로 즐기세요! (feat. Kafka streams)

안녕하세요. 카카오페이 정산플랫폼파트 와이입니다.

정산플랫폼파트에서 결제 데이터를 기반으로 정산에 필요한 데이터를 생성하는 역할을 담당하고 있습니다. 여러 채널에서 매일 수백만 건 이상의 결제 데이터를 수집하여 정산 데이터를 만들고 있으며, 대량의 데이터를 안전하고 효율적으로 처리하기 위해 Kafka를 활용하고 있습니다.

정산 데이터는 실시간으로 처리되지 않고, 매일 새벽 채널별 데이터 양에 따라 비실시간으로 생성되어 처리되고 있는데, 최근 파이프라인 방식으로 데이터를 처리하기 위해 Kafka Streams를 적용하면서, 기존에 비실시간으로 생성되던 정산 데이터를 실시간 스트림으로 생성하는 방안을 고민하게 되었습니다.

이 글에서는 기존 Kafka 인프라에서 별도의 클러스터 없이 스트림 처리 애플리케이션을 구축할 수 있는 Kafka Streams의 장점을 소개하고, 정산 데이터 생성 과정에 적용하는 과정을 공유하고자 합니다.

# Kafka Streams?

kafka streams는 kafka 위에서 동작하는 클라이언트 라이브러리로, 실시간 데이터를 스트림 형태로 처리하고 연속적으로 필터링(filtering), 변환(transform), 결합(joining), 집계(aggregating) 등의 작업을 연속적으로 수행할 수 있도록 지원합니다. 특히 로컬 상태 저장소(RocksDB)를 통해 복잡한 데이터의 상태를 효율적으로 관리할 수 있으며, 분산 처리 및 고가용성을 내장하여 복잡한 스트림 처리 애플리케이션을 간편하게 구축할 수 있습니다.
- 분산 처리: 여러 인스턴스가 작업을 분담하여 병렬로 처리함으로써 성능과 처리량을 향상시킵니다.
- 고가용성: 일부 인스턴스에 장애가 발생하더라도 다른 인스턴스가 작업을 이어받아 서비스 중단을 최소화합니다.

이제 kafka streams 적용에 앞서 가장 기본적이면서 핵심적인 개념인 `토폴로지`를 간략하게 살펴보겠습니다.

> kafka streams의 토폴로지는 **데이터 처리 흐름과 변환 과정을 정의하는 구조**입니다.

토폴로지는 다음과 같은 주요 구성 요소로 이루어져 있습니다.
- **프로세서(Processor)**: 토폴로지를 구성하는 노드로서, **데이터를 처리하는 역할**을 담당합니다.
  - **소스 프로세서**: kafka 토픽에서 데이터를 읽어와 처리의 **시작점**이 되는 노드입니다.
  - **스트림 프로세서**: 이전 프로세서로부터 전달받은 데이터를 필터링, 변환, 결합, 집계하는 등 실제 **데이터 처리 로직을 수행**하는 노드입니다.
  - **싱크 프로세서**: 처리된 데이터를 특정 kafka 토픽에 저장하는 **최종 노드**입니다.
- **스트림(Stream)**: 프로세서 노드들을 연결하는 선으로, 프로듀서/컨슈머의 레코드와 동일한 **토픽의 데이터를 의미**합니다.

![프로세서와 스트림](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-2.png?raw=true)

## 정산 데이터 생성 단계

실제 정산 데이터 생성 과정은 복잡하지만, 이해를 돕기 위해 kafka streams 적용 단계를 다음과 같이 간략하게 정리했습니다.

1. 결제 데이터 수신: kafka를 통해 결제 데이터를 받아옵니다.
2. 결제 메시지 저장: 결제 메시지를 로그로 저장합니다.
3. 베이스(건별 내역) 생성: 수신된 결제 데이터를 기반으로 건별 내역을 생성합니다.
4. 비정산/중복 결제건 필터링: 정산 대상이 아닌 결제 건을 걸러냅니다.
5. 지급룰 조회: 정산에 필요한 지급 규칙을 조회합니다.
6. 베이스(건별 내역) 저장: 처리된 건별 내역을 저장합니다.
7. 건별 내역 집계: 건별 내역을 집계하여 정산 금액을 계산합니다.
8. 집계 결과 전송: 정산 집계 결과(일정산)를 토픽으로 전송합니다.

# Kafka Streams 적용

일반적으로 kafka streams 애플리케이션은 다음 단계를 따라 개발합니다.

1. `StreamsConfig` 인스턴스 생성
2. `Serde` 객체 생성
3. 처리 토폴로지 구성
4. kafka streams 프로그램 시작

kafka streams를 적용하는 방법으로는 `Streams DSL`과 `Processor API` 두 가지가 있으며, 본 글에서는 `Streams DSL`(Domain Specific Language)을 사용하여 구현합니다.

두 방식의 차이점은 다음과 같습니다.

| Streams DSL                                                   |processor API|
|---------------------------------------------------------------|---|
| 일반적인 스트림 처리 작업을 위한 **고수준 추상화** 제공                           |스트림 처리 로직을 직접 정의하고 제어할 수 있는 **저수준 추상화** 제공|
| 필터링, 변환, 결합, 집계 등 **일반적인 스트림 처리 작업을 간단하고 선언적인 방식**으로 수행 |스트림 프로세서, 상태 저장소, 토폴로지 등을 **직접 정의하고 관리**|

Streams DSL 에서 제공하는 추상화된 모든 메서드는 [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html#)에서 확인할 수 있습니다.

이제 본격적으로 Kafka Streams를 적용해 보러 가볼까요~?🚗🚙🏎️

## StreamsConfig 인스턴스 생성

`StreamsConfig`에는 kafka 스트림즈 애플리케이션의 동작 방식을 정의하는 다양한 설정들이 들어갑니다.
- 애플리케이션의 기본 동작, kafka 클러스터 연결, 데이터 직렬화/역직렬화, 상태 관리, 장애 처리, 성능 튜닝 등

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    val streamsConfig = streamsConfig()
    // ..
}

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

여기서 사용할 kafka 스트림즈 애플리케이션의 설정을 살펴보겠습니다.

- `application.id`: kafka 스트림즈 애플리케이션의 **고유 식별자**입니다.
  - kafka 클러스터 내에서 유일해야 하며, **Kafka Consumer Group ID**로 사용됩니다.
- `bootstrap.servers`: kafka 브로커 서버의 주소 목록을 지정합니다.
  - 초기 연결을 위해 사용되며, `host:port` 형태로 쉼표로 구분하여 여러 개 지정이 가능합니다.
- `default.key.serde`: kafka 토픽에서 메시지를 읽거나 쓸 때 키(Key)의 기본 직렬화/역직렬화(Serde) 방식을 지정합니다.
- `default.value.serde`: kafka 토픽에서 메시지를 읽거나 쓸 때 값(Value)의 기본 직렬화/역직렬화(Serde) 방식을 지정합니다.
  - 메시지 키/값의 Serde 객체는 기본값 설정이 되어 있지 않으므로 명시적으로 설정해 주어야 합니다.
  - 커스텀한 Serde 객체를 사용할 수도 있습니다.
- `consumer.auto.offset.reset`: kafka 컨슈머의 오프셋을 설정합니다.

## 레코드 역직렬화를 위한 Serde 객체 생성

kafka에서 기본적으로 제공하는 [Serde](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html#available-serdes)를 사용하거나, 필요한 형태의 레코드를 사용하기 위해서 커스텀한 객체 생성이 필요합니다.<br/>
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
      - false: JSON 데이터에 역직렬화 대상 타입 객체에 정의되지 않은 속성이 있더라도 오류를 발생시키지 않고 해당 속성을 무시합니다.
      - true: 역직렬화 대상 타입 객체에 알 수 없는 속성이 있을 경우 역직렬화 과정에서 예외가 발생합니다.
- **신뢰할 수 있는 패키지 설정**
  - Jackson이 역직렬화를 수행할 때, 아무 클래스나 역직렬화하지 않도록 제한하는 기능입니다.
  - addTrustedPackages() 메서드를 사용하여 역직렬화가 허용되는 패키지 경로를 명시적으로 지정합니다.
- **`JsonSerde` 객체 생성 및 반환**
  - `JsonSerde`는 kafka 스트림즈에서 사용할 수 있도록 Serializer와 Deserializer를 하나로 묶은 클래스입니다.
  - 이렇게 생성된 `JsonSerde<StreamMessage<Payment>>` 객체는 kafka 스트림즈 토폴로지에서 `StreamMessage<Payment>` 타입의 데이터를 읽고 쓸 때 사용됩니다.

📚 [Kafka Streams Data Types and Serialization for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html#kstreams-data-types-and-serialization-for-cp)

## 처리 토폴로지 구성

kafka 스트림즈 적용을 위한 기본적인 준비는 되었습니다. 이제 생성하게 될 토폴로지의 구성을 살펴보겠습니다.

![처리 토폴로지 구성](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology-example.png?raw=true)

토폴로지를 정의하기 위해 먼저 `StreamsBuilder`라는 빌더 생성이 필요합니다.
- `StreamsBuilder`를 사용해서 여러 프로세서를 연결하고, 데이터 처리 파이프라인을 구축할 수 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    val builder = StreamsBuilder()
    // ...
}
```

이제 정산 데이터 생성을 위해 여섯 단계의 토폴로지를 한 개씩 만들어 보겠습니다.

### 1단계. 토픽으로부터 결제 데이터 받기

![토픽으로부터 결제 데이터 받기](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/source-processor.png?raw=true)

싱크 프로세서에 해당하는 `stream()` 메서드(input topics → KStream)는 토픽으로부터 소비한 메시지를 명시한 Serde 객체 형태에 맞게 매핑하고 레코드 스트림 [KStream](https://docs.confluent.io/platform/current/streams/concepts.html#kstream)을 생성합니다. `Serde`를 명시적으로 지정하지 않으면 streamsConfig 구성의 기본 Serde가 사용되고, Kafka 입력 토픽에 있는 레코드의 키/값 유형이 구성된 기본 Serde와 일치하지 않는 경우 Serde를 명시적으로 지정해야 합니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    val paymentStream: KStream<String, StreamMessage<Payment>> = builder.stream( // 입력 스트림(소스 프로세서)을 토폴로지에 추가
        kafkaProperties.paymentTopic, // 데이터를 읽어올 Kafka 토픽 이름
        Consumed.with( // 키와 값의 직렬화/역직렬화기(Serde) 지정
            Serdes.String(),
            serdeFactory.messagePaymentSerde() // Serde 객체 생성 단계에서 생성한 Serde를 사용
        )
    )
    // ...
}
```

토폴로지에 추가된 스트림이 정상적으로 동작하는지 확인하고 싶다면, 디버깅/테스트 환경에서 [print()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#print-org.apache.kafka.streams.kstream.Printed-) 메서드를 활용해서 단계별로 레코드의 상태를 확인할 수도 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    paymentStream 
        .print(Printed.toSysOut<String, StreamMessage<Payment>>().withLabel("payment-stream"))
        // [payment-stream]: 5a54041d-2cce-43f5-8194-299acb8e8766, StreamMessage(channel=OFFLINE, action=PAYMENT, data=Payment(paymentType=OFFLINE, amount=65218, payoutDate=2025-05-21, confirmDate=2025-05-21, merchantNumber=merchant-1881, paymentDate=2025-05-19T21:48:15.989609, paymentActionType=PAYMENT, paymentMethodType=CARD))
  // ...
}
```

### 2단계. 결제 메시지 저장

![결제 메시지 저장](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-peek.png?raw=true)

`stream` 메서드를 통해 수신되는 결제 데이터를 [peek()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#peek-org.apache.kafka.streams.kstream.ForeachAction-) 연산에 적용된 람다 함수를 통해 로그에 저장합니다. `peek` 메서드는 각 레코드에 대해 작업을 수행하고 변경되지 않은 스트림을 반환합니다. peek는 로깅이나 메트릭 추적, 디버깅 및 트러블슈팅과 같은 상황에 유용하게 사용할 수 있습니다. 만일 스트림 데이터에 대한 수정 작업이 필요할 경우 `map`, `mapValues` 같은 메서드를 사용할 수 있습니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    paymentStream // 스트림을 통해 들어오는 모든 결제 메시지를 로그로 저장
        .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
    // ...
}
```

### 3단계. 결제 데이터로 정산 베이스 생성

![결제 데이터로 정산 베이스 생성](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-mapValue.png?raw=true)

[mapValues()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#map-org.apache.kafka.streams.kstream.KeyValueMapper-) 메서드를 통해 스트림의 각 레코드에 대해 키는 그대로 유지하면서, 값만을 새로운 타입(`Base`)으로 변환합니다. 변환 로직은 인자로 전달된 `ValueMapper` 인터페이스의 구현체에 의해 정의됩니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    paymentStream
        .mapValues(BaseMapper())
    // ...
}
```

`mapValues` 메서드에 전달하기 위한 `ValueMapper` 구현체를 정의해 보겠습니다. `ValueMapper<V, VR>` 인터페이스는 입력 값 타입 `V`를 출력 값 타입 `VR`로 변환하는 역할을 합니다. 여기서 입력 값 타입 `V`는 `StreamMessage<Payment>`, 출력 값 타입 `VR`은 `Base`에 해당하고, 기존 스트림의 값을 어떻게 변환할지에 대한 구체적인 로직을 정의합니다.

```kotlin
class BaseMapper() : ValueMapper<StreamMessage<Payment>, Base> { // ValueMapper 인터페이스 구현
  // 스트림의 각 메시지에 대해 apply 메서드를 호출하며, 메시지의 값을 인자로 전달
  override fun apply(payment: StreamMessage<Payment>): Base {
    return Base( // 입력으로 받은 객체의 데이터를 사용하여 새로운 객체를 생성하고 반환
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

### 4단계. 비정산 또는 중복 결제건 필터링

![비정산 또는 중복 결제건 필터링](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-filter.png?raw=true)

베이스가 생성된 후, 결제 데이터 중에서 비정산(테스트 결제, 비정산 가맹점, 망 취소, 미확인 등) 또는 중복된 건들은 UnSettlement, Duplicated로 분류합니다. 이렇게 분류된 데이터 중 정산 대상에 해당하는 데이터만 다음 파이프라인으로 이어지도록 [filter()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#filter-org.apache.kafka.streams.kstream.Predicate-) 메서드를 사용합니다.

원칙적으로 비정산 결제 건과 중복 결제 건 필터링은 각각 별도의 프로세서로 구현하는 것이 더 명확하겠지만, 이 글에서는 설명의 간결함을 위해 하나의 단계로 합쳤습니다. filter 메서드는 주어진 조건을 만족하는 레코드만으로 구성된 새로운 KStream을 반환하며, 조건을 만족하지 않는 레코드는 스트림에서 제외됩니다.

```kotlin
// SettlementKafkaStreamsApp.kt
@Bean
fun settlementStreams(): KafkaStreams {
    // ...
    paymentStream
        .filter { _, base -> settlementService.isSettlement(base) }
    // ...
}
```

### 5단계. 지급룰 조회 및 세팅

![지급룰 조회 및 세팅](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-processValues.png?raw=true)

이제 필터링된 정산 대상 데이터에 지급룰 정보를 설정할 차례입니다. 지급룰은 API 호출을 통해 조회하는데, 동일한 규칙을 사용하는 데이터에 대해 중복 API 호출을 방지하고 네트워크 통신 비용을 절감하기 위해 지급룰을 별도로 관리하고자 합니다.

이러한 요구사항을 해결하기 위해 `Redis`를 사용할 수도 있지만, 여기서는 Kafka Streams의 `상태 저장소`를 활용해 보겠습니다. 상태 저장소는 `RocksDB`와 같은 로컬 저장소를 사용하여 `KTable` 형태로 키-값 데이터를 관리하며, `변경 로그 토픽`을 통해 상태를 복원하여 내결함성을 보장합니다. 이렇게 구성된 상태 저장소와 연동하여 레코드를 개별적으로 처리하기 위해 [processValues()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#processValues-org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier-java.lang.String...-) 메서드를 사용합니다.

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

|           | KTable                                                       | GlobalKTable                                                               |
|-----------|--------------------------------------------------------------|----------------------------------------------------------------------------|
| 데이터 저장 방식 | 데이터 분산 저장 (각 파티션의 데이터는 해당 파티션을 담당하는 인스턴스에 저장)                                                       | 모든 인스턴스의 로컬 상태 저장소에 전체 데이터 복제 |
| 데이터 조회    | 특정 키 조회 시, 해당 키가 속한 파티션을 알아야 하며, 데이터가 다른 인스턴스에 있다면 네트워크를 통한 조회가 필요 | 각 인스턴스가 전체 데이터의 로컬 복사본을 가지므로, 파티션에 관계없이 어떤 키든 로컬에서 빠르게 조회 가능         |
| 변경 로그     | 소스 토픽의 파티션과 1:1로 매핑되는 별도의 내부 변경 로그 토픽을 생성하고 사용                                 | 소스 토픽 자체가 변경 로그 역할을 하며, 모든 인스턴스가 이 토픽을 구독하여 로컬 상태를 최신으로 유지            |

이 글에서는 지급룰을 상태 저장소에 저장하기 위해 `GlobalKTable`을 사용했습니다. `GlobalKTable`은 `KTable`과 달리 모든 인스턴스가 동일한 소스 토픽을 변경 로그로 사용합니다. 따라서 각 인스턴스는 소스 토픽에서 데이터를 읽어와 자신의 로컬 상태 저장소를 업데이트하고, 이를 통해 모든 인스턴스가 전체 데이터셋의 완전한 복제본을 로컬에 유지하게 됩니다. 그 결과, 파티션에 관계없이 로컬에서 신속한 조회가 가능해집니다.

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

이 글에서 다룬 예제가 여러분의 스트리밍 여정에 작은 영감이 되기를 바랍니다. 지금 바로 Kafka Streams의 강력함을 경험해 보세요!

## 참고
- [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html)
- [Kafka Streams Architecture for Confluent Platform](https://docs.confluent.io/platform/current/streams/architecture.html)
- [Kafka Streams Interactive Queries for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/interactive-queries.html)

