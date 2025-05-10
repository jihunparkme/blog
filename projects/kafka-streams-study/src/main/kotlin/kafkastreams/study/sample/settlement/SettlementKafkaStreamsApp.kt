package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.config.KafkaProperties
import kafkastreams.study.sample.settlement.config.KafkaStreamsConfig
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.processor.PayoutRuleProcessValues
import kafkastreams.study.sample.settlement.service.SettlementService
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Printed
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
    private final val PAYOUT_RULE_STATE_STORE_NAME = "payoutRulesStore"

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

        // [소스 프로세서] 결제 토픽으로부터 결제 데이터 받기
        val paymentStream = builder.stream(
            kafkaProperties.paymentTopic,
            Consumed.with(
                keySerde,
                valueSerde
            )
        )
        println("============================")

        val storeBuilder = getPayoutDateStoreBuilder()
        builder.addStateStore(storeBuilder)

        paymentStream
            // TODO: filter 로 finish 아닌 것만
            // [스트림 프로세서] 결제 메시지 로그 저장
            .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
            .processValues(PayoutRuleProcessValues(PAYOUT_RULE_STATE_STORE_NAME, payoutRuleClient), PAYOUT_RULE_STATE_STORE_NAME)
            .print(Printed.toSysOut<String, Payment>().withLabel("payment-stream"))

        /**
         * [스트림 프로세서]
         * - 룰 조회(state store 활용 룰 관리)
         * - 없을 경우 API 조회
         *
         * [스트림 프로세서]
         * - 베이스 생성 valueTransform
         *
         * [스트림 프로세서]
         * - 베이스 저장
         *
         * [스트림 프로세서]
         * - 결제 데이터 집계(Group By key)
         * - state store
         *
         * [스트림 프로세서]
         * - 각 파티션에 FINISH 메시지가 도착하면 집계로 된 일통계 저장
         *
         * GlobalKTable(활용?) 환율?
         *
         * 스트림 과정을 printed 로 확인
         */
        return KafkaStreams(builder.build(), streamsConfig)
    }

    private fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
        val streamMessagePaymentDeserializer = JsonDeserializer(
            object : TypeReference<StreamMessage<Payment>>() {},
            objectMapper,
            false) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
        streamMessagePaymentDeserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.common.*",
            "kafkastreams.study.sample.settlement.domain.*",
        )

        return JsonSerde(
            JsonSerializer(objectMapper),
            streamMessagePaymentDeserializer
        )
    }

    private fun getPayoutDateStoreBuilder(): StoreBuilder<KeyValueStore<String?, StreamMessage<Payment>?>?>? {
        val storeSupplier = Stores.inMemoryKeyValueStore(PAYOUT_RULE_STATE_STORE_NAME)
        val storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), messagePaymentSerde())
        return storeBuilder
    }
}
