# 스트림하게 정산 데이터 생성하기

정산플랫폼팀에서 결제 데이터를 기반으로 정산에 필요한 정산 데이터를 생성하는 역할을 맡고 있습니다.

정산 데이터는 Kafka를 통해 수신한 결제 데이터로 여러 단계를 거쳐 베이스(건별 내역)/통계(일별 내역)라고 불리는 데이터로 생성됩니다.<br/>
여러 채널에 대한 결제 데이터를 받기 위해 Kafka를 사용하면서 Kafka Streams 라는 기술에 대해 알게 되었고,<br/>
데이터 수신부터 정산 데이터 생성까지의 복잡한 단계를 Kafka Streams를 통해 스트림 파이프라인 방식으로 처리되도록<br/>
한 단계씩 적용해 보며 Kafka Streams에 대한 매력을 함께 공유하고자 합니다.

# Kafka Streams?

카프카 스트림즈(Kafka Streams)를 한 줄로 표현한다면 아래와 같이 표현할 수 있습니다.

> 토픽에 적재된 데이터를 상태 또는 비상태기반으로 실시간 변환하여 다른 토픽에 적재하는 라이브러리

카프카 스트림즈는 데이터 스트림을 필터링, 변환, 결합, 집계하는 등의 복잡한 스트림 처리 로직을 간결하고 직관적인 코드로 구현할 수 있고,<br/>
무엇보다 기존 카프카를 사용하고 있다면 카프카 자체의 인프라를 활용하여 스트림 처리 애플리케이션을 구축할 수 있습니다.

더 자세한 설명은 코드와 함께 설명하고, 먼저 기존 방식과 함께 어떻게 카프카 스트림즈를 적용할지 함께 고민해 보려고 합니다.

# 정산 데이터 생성 단계

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

# Kafka Streams 적용

이제 위에 나열된 각 시나리오에 Kafka streams를 적용해 보겠습니다.<br/>

전체 코드를 쉽게 이해하기 위해 먼저 각 단계를 하나씩 살펴보려고 합니다.

카프카 스트림즈 애플리케이션을 만들기 위해 사용되는 일반적인 패턴을 하나씩 적용해 보겠습니다.

1. StreamsConfig 인스턴스 생성
2. Serde 객체 생성
3. 처리 토폴로지 구성
4. 카프카 스트림즈 프로그램 시작

먼저 전체 코드를 살짝 보고 시작해 보겠습니다.

https://kafka.apache.org/30/documentation/streams/developer-guide/dsl-api.html#id10

Streams DSL 을 활용한 방식

Streams DSL(Domain Specific Language)
일반적인 스트림 처리 작업을 위한 고수준의 추상화를 제공
필터링, 매핑, 집계, 조인 등과 같은 일반적인 스트림 처리 작업을 간단하고 선언적인 방식으로 수행

더 낮은 수준의 추상화는
processor API
스트림 처리 로직을 직접 정의하고 제어할 수 있는 낮은 수준의 추상화를 제공
스트림 프로세서, 상태 저장소, 토폴로지 등을 직접 정의하고 관리

```kotlin

```

## StreamsConfig 인스턴스 생성

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

## 레코드 역직렬화를 위한 Serde 객체 생성

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

## 처리 토폴로지 구성

> 데이터 스트림을 처리하는 과정, 즉 데이터의 흐름과 변환 과정을 정의하는 구조

하나 이상의 카프카 토픽에서 데이터를 가져오려면 소스 프로세스에 해당하는 토폴로지 생성이 필요합니다.

`processor`: 카프카 스트림즈에서 토폴로지를 이루는 노드
- 프로세서에서 소스 프로세서, 스트림 프로세서, 싱크 프로세서 세 가지가 존재

`stream`: 노드와 노드를 이은 선
- 스트림은 토픽의 데이터를 뜻하는데 프로듀서와 컨슈머에서의 레코드와 동일

⁉️ 토폴리지를 구성한 그림

**소스 프로세스**

데이터를 처리하기 위해 최초로 선언해야 하는 노드
하나 이상의 토픽에서 데이터를 가져오는 역할

**스트림 프로세스**

다른 프로세서가 반환한 데이터를 처리하는 역할
변환, 분기처리와 같은 로직이 데이터 처리의 일종

**싱크 프로세서**

데이터를 특정 카프카 토픽으로 저장하는 역할
스트림즈로 처리된 데이터의 최종 종착지

### 0️⃣ 빌더 생성

StreamsBuilder 역할과 StateStore 대한 설명

```kotlin
val builder = StreamsBuilder()
        // 지급룰 stateStore 추가
        builder.addStateStore(getPayoutDateStoreBuilder())

private fun getPayoutDateStoreBuilder(): StoreBuilder<KeyValueStore<String, Rule>> {
    val storeSupplier = Stores.inMemoryKeyValueStore(PAYOUT_RULE_STATE_STORE_NAME)
    return Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), serdeFactory.ruleSerde())
}
```

### 1️⃣ 결제팀으로부터 결제 데이터 수신

이 영역은 소스 프로세서

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
### 2️⃣ 결제 메시지 로그 저장

스트림 프로세서

⁉️ peek 메서드에 대한 설명

```kotlin
paymentStream
    .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
```

### 3️⃣ FINISH 메시지 필터링

⁉️ filter 메서드에 대한 설명

필터 사용을 위한 가정

```kotlin
paymentStream
    .filter { _, message -> message.action != Type.FINISH }
```

### 4️⃣ 결제 데이터로 정산 베이스 생성

스트림 프로세서

⁉️ mapValues 메서드에 대한 설명

```kotlin
paymentStream
    .mapValues(BaseMapper())
```

Mapper 구현

```kotlin
class BaseMapper() : ValueMapper<StreamMessage<Payment>, Base> {
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

### 5️⃣ 비정산 결제건 필터링

스트림 프로세서

```kotlin
.filter { _, base -> base.isNotUnSettlement() }
```

### 5️⃣ 지급룰 조회 및 세팅

스트림 프로세서

⁉️ processValues 메서드에 대한 설명

```kotlin
paymentStream
    .processValues(
        PayoutRuleProcessValues(PAYOUT_RULE_STATE_STORE_NAME, payoutRuleClient),
        PAYOUT_RULE_STATE_STORE_NAME
    )
```

PayoutRuleProcessValues 구현

```kotlin
class PayoutRuleProcessValues(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient,
) : FixedKeyProcessorSupplier<String, Base, Base> {
    override fun get(): FixedKeyProcessor<String, Base, Base> {
        return PayoutRuleProcessor(stateStoreName, payoutRuleClient)
    }
}

class PayoutRuleProcessor(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient
) : FixedKeyProcessor<String, Base, Base> {
    private var context: FixedKeyProcessorContext<String, Base>? = null
    private var payoutRuleStore: KeyValueStore<String, Rule>? = null

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
        var rule = payoutRuleStore?.get(stateStoreName)
        // stateStore에 지급룰이 저장되어 있지 않을 경우 API 요청 후 저장
        if (rule == null) {
            log.info(">>> [지급룰 조회] Search payout rule.. $key")
            val findRule = payoutRuleClient.getPayoutDate(
                PayoutDateRequest(
                    merchantNumber = base.merchantNumber ?: throw IllegalArgumentException(),
                    paymentDate = base.paymentDate,
                    paymentActionType = base.paymentActionType ?: throw IllegalArgumentException(),
                    paymentMethodType = base.paymentMethodType ?: throw IllegalArgumentException(),
                )
            )
            payoutRuleStore?.put(stateStoreName, findRule)
            rule = findRule
        }

        // 가맹점에 대한 지급룰이 없을 경우
        if (rule == null) {
            log.info(">>> [지급룰 없음] Not found payment payout rule. key: $key")
            base.updateDefaultPayoutDate()
        }

        // 지급룰 업데이트 대상일 경우
        if (rule != null && (rule.payoutDate != base.payoutDate || rule.confirmDate != base.confirmDate)) {
            log.info(">>> [지급룰 정보 저장] Save payout date.. $key")
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

### 6️⃣ 정산 베이스 저장

스트림 프로세서

```kotlin
paymentStream
    .peek({ _, message -> settlementService.saveBase(message) })
```

### 7️⃣ 집계

```kotlin
val statisticsTable = baseStream.groupBy(
    { _, base ->
        BaseAggregationKey( // Base 에서 복합 키 추출
            merchantNumber = base.merchantNumber,
            paymentDateDaily = base.paymentDate.toLocalDate(),
            paymentActionType = base.paymentActionType,
            paymentMethodType = base.paymentMethodType
        )
    },
    Grouped.with( // 그룹화에 사용될 복합 키, 원본 Base 를 위한 Serdes 지정
        serdeFactory.baseAggregationKeySerde(),
        serdeFactory.baseSerde()
    )
)
    .aggregate( // 그룹별로 집계 수행
        { // 각 그룹의 집계가 시작될 때 초기값을 반환
            BaseAggregateValue()
        },
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

## 카프카 스트림즈 프로그램 시작

```kotlin
KafkaStreams(builder.build(), streamsConfig)
```

⁉️ stateStore 어떤 구조로 저장되는지

⁉️ 조회는 어떤 방식으로 하는지
- 조회하면 어떤 형태로 응답이 오는지