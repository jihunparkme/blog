package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.common.Type
import kafkastreams.study.sample.settlement.config.KafkaProperties
import kafkastreams.study.sample.settlement.config.KafkaStreamsConfig
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregateValue
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregationKey
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
import kafkastreams.study.sample.settlement.domain.settlement.Base
import kafkastreams.study.sample.settlement.service.SettlementService
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class SettlementKafkaStreamsApp(
    private val streamsConfig: KafkaStreamsConfig,
    private val kafkaProperties: KafkaProperties,
    private val objectMapper: ObjectMapper,
    private val settlementService: SettlementService,
    private val payoutRuleClient: PayoutRuleClient,
) {
    private final val PAYOUT_RULE_STATE_STORE_NAME = "payout-rules-store"
    private final val STATISTICS_STORE_NAME = "statistics-store"

    @Bean
    fun settlementStreams(): KafkaStreams {
        /*************************************
         * 1. StreamsConfig 인스턴스 생성
         */
        val streamsConfig = streamsConfig.properties(kafkaProperties.paymentApplicationName)

        /*************************************
         * 2. 레코드 역직렬화를 위한 Serde 객체 생성
         */
        val keySerde = Serdes.String()
        val valueSerde = messagePaymentSerde()

        /*************************************
         * 3. 처리 토폴로지 구성
         */
        val builder = StreamsBuilder()
        // 지급룰 stateStore 추가
        builder.addStateStore(getPayoutDateStoreBuilder())

        // [소스 프로세서] 결제 토픽으로부터 결제 데이터 받기
        val paymentStream = builder.stream(
            kafkaProperties.paymentTopic,
            Consumed.with(
                keySerde,
                valueSerde
            )
        )

        println("============================")
        val baseStream = paymentStream
            // [스트림 프로세서] 결제 메시지 로그 저장
            .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
            // [스트림 프로세서] FINISH 메시지는 로그만 저장
            .filter { _, message -> message.action != Type.FINISH }
            // [스트림 프로세서] 결제 데이터로 정산 베이스 생성
            .mapValues(BaseMapper())
            // [스트림 프로세서] 지급룰 조회 및 세팅
            .processValues(
                PayoutRuleProcessValues(PAYOUT_RULE_STATE_STORE_NAME, payoutRuleClient),
                PAYOUT_RULE_STATE_STORE_NAME
            )
            // [스트림 프로세서] 정산 베이스 저장
            .peek({ _, message -> settlementService.saveBase(message) })
        // .print(Printed.toSysOut<String, Base>().withLabel("payment-stream"))

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
                baseAggregationKeySerde(),
                baseSerde()
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
                    .withKeySerde(baseAggregationKeySerde())   // KTable의 키(BaseAggregationKey) Serde
                    .withValueSerde(baseAggregateValueSerde()) // KTable의 값(BaseAggregateValue) Serde
            )

        /*************************************
         * 4. 카프카 스트림즈 인스턴스 생성
         */
        return KafkaStreams(builder.build(), streamsConfig)
    }

    private fun getPayoutDateStoreBuilder(): StoreBuilder<KeyValueStore<String, Rule>> {
        val storeSupplier = Stores.inMemoryKeyValueStore(PAYOUT_RULE_STATE_STORE_NAME)
        return Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), ruleSerde())
    }

    // TODO: Serde 메서드 추출
    private fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
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

    private fun baseSerde(): JsonSerde<Base> {
        val streamMessagePaymentDeserializer = JsonDeserializer(
            object : TypeReference<Base>() {},
            objectMapper,
            false
        )
        streamMessagePaymentDeserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.common.*",
            "kafkastreams.study.sample.settlement.domain.*",
        )

        return JsonSerde(
            JsonSerializer(objectMapper),
            streamMessagePaymentDeserializer
        )
    }

    private fun ruleSerde(): JsonSerde<Rule> {
        val streamMessagePaymentDeserializer = JsonDeserializer(
            object : TypeReference<Rule>() {},
            objectMapper,
            false
        ) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
        streamMessagePaymentDeserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.*",
        )

        return JsonSerde(
            JsonSerializer(objectMapper),
            streamMessagePaymentDeserializer
        )
    }

    private fun baseAggregationKeySerde(): JsonSerde<BaseAggregationKey> {
        val deserializer = JsonDeserializer(
            object : TypeReference<BaseAggregationKey>() {},
            objectMapper,
            false
        )
        deserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.aggregation.*",
            "kafkastreams.study.sample.settlement.common.*"
        )
        return JsonSerde(JsonSerializer(objectMapper), deserializer)
    }

    private fun baseAggregateValueSerde(): JsonSerde<BaseAggregateValue> {
        val deserializer = JsonDeserializer(
            object : TypeReference<BaseAggregateValue>() {},
            objectMapper,
            false
        )
        deserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.aggregation.*"
        )
        return JsonSerde(JsonSerializer(objectMapper), deserializer)
    }
}