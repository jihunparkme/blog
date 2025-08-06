# 정산 데이터, 이제 스트리밍으로 즐기세요! (feat. Kafka streams)

정산 데이터는 실시간으로 처리되지 않고, 매일 새벽 채널별 데이터 양에 따라 비실시간으로 생성되어 처리되고 있는데, 최근 파이프라인 방식으로 데이터를 처리하기 위해 Kafka Streams를 적용하면서, 기존에 비실시간으로 생성되던 정산 데이터를 실시간 스트림으로 생성하는 방안을 고민하게 되었습니다.
<br/>

이 글에서는 기존 Kafka 인프라에서 별도의 클러스터 없이 스트림 처리 애플리케이션을 구축할 수 있는 Kafka Streams의 장점을 소개하고, 정산 데이터 생성 과정에 적용하는 과정을 공유하고자 합니다.

# Kafka Streams?

kafka streams는 kafka 위에서 동작하는 클라이언트 라이브러리로, 실시간 데이터를 스트림 형태로 처리하고 연속적으로 필터링(filtering), 변환(transform), 결합(joining), 집계(aggregating) 등의 작업을 연속적으로 수행할 수 있도록 지원합니다. 특히 로컬 상태 저장소(RocksDB)를 통해 복잡한 데이터의 상태를 효율적으로 관리할 수 있으며, 분산 처리 및 고가용성을 내장하여 복잡한 스트림 처리 애플리케이션을 간편하게 구축할 수 있습니다.
- 분산 처리: 여러 인스턴스가 작업을 분담하여 병렬로 처리함으로써 성능과 처리량을 향상시킵니다.
- 고가용성: 일부 인스턴스에 장애가 발생하더라도 다른 인스턴스가 작업을 이어받아 서비스 중단을 최소화합니다.
<br/>

이제 kafka streams 적용에 앞서 가장 기본적이면서 핵심적인 개념인 `토폴로지`를 간략하게 살펴보겠습니다.
<br/>

> kafka streams의 토폴로지는 **데이터 처리 흐름과 변환 과정을 정의하는 구조**입니다.

<br/>

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
4. 비정산/중복 결제 건 필터링: 정산 대상이 아닌 결제 건을 걸러냅니다.
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
<br/>

kafka streams를 적용하는 방법으로는 `Streams DSL`과 `Processor API` 두 가지가 있으며, 본 글에서는 `Streams DSL`(Domain Specific Language)을 사용하여 구현합니다.
<br/>

두 방식의 차이점은 다음과 같습니다.

| Streams DSL                                                                                 | processor API                                                          |
| ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| 일반적인 스트림 처리 작업을 위한 **고수준 추상화** 제공                                     | 스트림 처리 로직을 직접 정의하고 제어할 수 있는 **저수준 추상화** 제공 |
| 필터링, 변환, 결합, 집계 등 **일반적인 스트림 처리 작업을 간단하고 선언적인 방식**으로 수행 | 스트림 프로세서, 상태 저장소, 토폴로지 등을 **직접 정의하고 관리**     |

Streams DSL 에서 제공하는 추상화된 모든 메서드는 [Kafka Streams Domain Specific Language for Confluent Platform](https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html#)에서 확인할 수 있습니다.
<br/>

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

kafka에서 기본적으로 제공하는 [Serde](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html#available-serdes)를 사용하거나, 필요한 형태의 레코드를 사용하기 위해서 커스텀한 객체 생성이 필요합니다.
<br/>

여기서는 Json 형태의 `StreamMessage<Payment>` 객체로 메시지 값을 역직렬화화기 위해 커스텀한 Serde 객체를 생성해 보겠습니다.

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

### 4단계. 비정산 또는 중복 결제 건 필터링

![비정산 또는 중복 결제 건 필터링](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/example-filter.png?raw=true)

베이스가 생성된 후, 결제 데이터 중에서 비정산(테스트 결제, 비정산 가맹점, 망 취소, 미확인 등) 또는 중복된 건들은 UnSettlement, Duplicated로 분류합니다. 이렇게 분류된 데이터 중 정산 대상에 해당하는 데이터만 다음 파이프라인으로 이어지도록 [filter()](https://docs.confluent.io/platform/7.9/streams/javadocs/javadoc/org/apache/kafka/streams/kstream/KStream.html#filter-org.apache.kafka.streams.kstream.Predicate-) 메서드를 사용합니다.
<br/>

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

.
.
.
<br/>


5단계부터는 2편에서 찾아뵙겠습니다~! 👋🏼
<br/>

[정산 데이터, 이제 스트리밍으로 즐기세요! (feat. Kafka streams) 2편](https://data-make.tistory.com/803)