# Kafka Streams 로 실시간으로 정산 데이터 생성하기

정산플랫폼팀에서 결제 데이터를 기반으로 정산에 필요한 베이스, 통계 데이터를 생성하는 역할을 맡고 있습니다.<br/>
결제팀으로부터 Kafka를 통해 수신한 결제 데이터는 여러 단계를 거쳐 지급, 전표 생성, 세금계산서 발행 등 정산에 필요한 베이스 및 통계 데이터로 생성됩니다.<br/>
이러한 여러 단계의 데이터 처리 과정을 Kafka Streams의 파이프라인 형태로 재구성하여, 데이터를 스트림 방식으로 처리하도록 적용해 보려고 합니다.<br/>

## Kafka Streams?

카프카 스트림즈(Kafka Streams)를 한 줄로 표현한다면 아래와 같이 표현할 수 있습니다.

> 토픽에 적재된 데이터를 상태 또는 비상태기반으로 실시간 변환하여 다른 토픽에 적재하는 라이브러리

카프카 스트림즈는 데이터 스트림을 필터링, 변환, 결합, 집계하는 등의 복잡한 스트림 처리 로직을 간결하고 직관적인 코드로 구현할 수 있고,<br/>
무엇보다 기존 카프카를 사용하고 있다면 카프카 자체의 인프라를 활용하여 스트림 처리 애플리케이션을 구축할 수 있습니다.

더 자세한 설명은 코드와 함께 설명하고, 먼저 기존 방식과 함께 어떻게 카프카 스트림즈를 적용할지 함께 고민해 보려고 합니다.

## 정산 데이터 생성 단계

정산 베이스 및 통계 데이터 생성에 kafka streams 적용을 위해 먼저 간단한 시나리오를 만들어 보았습니다.

정산 데이터 생성을 위해 보다 더 복잡한 비즈니스 로직들이 포함되어 있지만 kafka streams 적용에 초점을 맞추고자 단순한 시나리오로 진행해 보려고 합니다. 

1. 결제팀으로부터 결제 데이터 수신
2. 베이스(건별 내역) 생성
   - 중복 베이스 확인
   - 테스트 데이터 확인 / 비정산 결제건 확인 / 정산 대상 확인(망취소, 미확인..)
   - 정산 대상일 경우 정산 룰(지급 규칙) 조회
3. 베이스(건별 내역) 저장
4. 승인일 통계 생성
- 통계 메시지 전송
- 통계 메시에 따른 upsert

## Kafka Streams 적용

이제 위에 나열된 각 시나리오에 Kafka streams를 적용해 보겠습니다.<br/>

전체 코드를 쉽게 이해하기 위해 먼저 각 단계를 하나씩 살펴보려고 합니다.

카프카 스트림즈 애플리케이션을 만들기 위해 사용되는 일반적인 패턴을 하나씩 적용해 보겠습니다.

1. StreamsConfig 인스턴스 생성
2. Serde 객체 생성
3. 처리 토폴로지 구성
4. 카프카 스트림즈 프로그램 시작

### StreamsConfig 인스턴스 생성

⁉️StreamsConfig 에 어떤 설정이 들어가는지

```kotlin
val streamsConfig = streamsConfig.properties(kafkaProperties.paymentApplicationName)
```

streamsConfig 빈을 들여다 보면

```kotlin
@Configuration
class KafkaStreamsConfig {
    fun properties(applicationId: String): Properties {
        return Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
    }
}
```

### 레코드 역직렬화를 위한 Serde 객체 생성

```kotlin
val keySerde = Serdes.String()
val valueSerde = serdeFactory.messagePaymentSerde()
```

⁉️ json 형태의 레코드를 받기 위해 serde 객체 생성이 필요 

```kotlin
fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
    val streamMessagePaymentDeserializer = JsonDeserializer(
        object : TypeReference<StreamMessage<Payment>>() {},
        objectMapper,
        false
    ) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
    streamMessagePaymentDeserializer.addTrustedPackages(
        "kafkastreams.study.sample.settlement.common.*",
        "kafkastreams.study.sample.settlement.domain.*",
    )

    return JsonSerde(
        JsonSerializer(objectMapper),
        streamMessagePaymentDeserializer
    )
}
```






### 처리 토폴로지 구성

결제팀으로부터 결제 데이터 수신

하나 이상의 카프카 토픽에서 데이터를 가져오려면 소스 프로세스에 해당하는 토폴로지 생성이 필요합니다.

```kotlin
// [소스 프로세서] 결제 토픽으로부터 결제 데이터 받기
val paymentStream = builder.stream(
   kafkaProperties.paymentTopic,
   Consumed.with(
       keySerde,
       valueSerde
   )
)
```






