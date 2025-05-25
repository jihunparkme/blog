package kafkastreams.study.sample.settlement

import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.config.KafkaProperties
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregateValue
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregationKey
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
import kafkastreams.study.sample.settlement.service.SettlementService
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import java.util.Properties

@Configuration
class SettlementKafkaStreamsApp(
    private val kafkaProperties: KafkaProperties,
    private val serdeFactory: SerdeFactory,
    private val settlementService: SettlementService,
    private val payoutRuleClient: PayoutRuleClient,
    private val ruleKafkaTemplate: KafkaTemplate<String, Rule>,
) {
    private final val GLOBAL_PAYOUT_RULE_STATE_STORE_NAME = "global-payout-rules-store"
    private final val STATISTICS_STORE_NAME = "statistics-store"

    @Bean
    fun settlementStreams(): KafkaStreams {
        /*************************************
         * 1. StreamsConfig 인스턴스 생성
         */
        val streamsConfig = streamsConfig()

        /*************************************
         * 3. 처리 토폴로지 구성
         */
        val builder = StreamsBuilder()
        applyGlobalTable(builder)

        // [소스 프로세서] 결제 토픽으로부터 결제 데이터 받기
        val paymentStream: KStream<String, StreamMessage<Payment>> = builder.stream(
            kafkaProperties.paymentTopic,
            Consumed.with(
                Serdes.String(),
                serdeFactory.messagePaymentSerde()
            )
        )

        val baseStream = paymentStream
            // [스트림 프로세서] 결제 메시지 로그 저장
            .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
            // [스트림 프로세서] 결제 데이터로 정산 베이스 생성
            .mapValues(BaseMapper())
            // // [스트림 프로세서] 비정산 결제건 필터링
            .filter { _, base -> settlementService.isSettlement(base) }
            // // [스트림 프로세서] 지급룰 조회 및 세팅
            .processValues(
                PayoutRuleProcessValues(
                    rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
                    stateStoreName = GLOBAL_PAYOUT_RULE_STATE_STORE_NAME,
                    payoutRuleClient = payoutRuleClient,
                    ruleKafkaTemplate = ruleKafkaTemplate,
                ),
            )
            // // [스트림 프로세서] 정산 베이스 저장
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

        /*************************************
         * 4. 카프카 스트림즈 인스턴스 생성
         */
        return KafkaStreams(builder.build(), streamsConfig)
    }

    private fun applyGlobalTable(builder: StreamsBuilder) {
        builder.globalTable(
            kafkaProperties.paymentRulesGlobalTopic,
            Materialized.`as`<String, Rule, KeyValueStore<Bytes, ByteArray>>(GLOBAL_PAYOUT_RULE_STATE_STORE_NAME)
                .withKeySerde(Serdes.String())
                .withValueSerde(serdeFactory.ruleSerde())
        )
    }

    @Bean
    fun streamsConfig(): StreamsConfig =
        StreamsConfig(Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.paymentApplicationName)
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.servers)
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, serdeFactory.messagePaymentSerde().javaClass)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        })
}