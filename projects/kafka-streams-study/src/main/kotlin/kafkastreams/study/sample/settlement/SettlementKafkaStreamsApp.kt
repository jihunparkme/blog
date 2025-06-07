package kafkastreams.study.sample.settlement

import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
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
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime
import java.util.Properties

@Configuration
class SettlementKafkaStreamsApp(
    private val kafkaProperties: KafkaProperties,
    private val serdeFactory: SerdeFactory,
    private val settlementService: SettlementService,
    private val payoutRuleClient: PayoutRuleClient,
    private val ruleKafkaTemplate: KafkaTemplate<String, Rule>,
) {
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
            // [스트림 프로세서] 지급룰 조회 및 세팅
            .processValues(
                PayoutRuleProcessValues(
                    rulesGlobalTopic = kafkaProperties.paymentRulesGlobalTopic,
                    stateStoreName = kafkaProperties.globalPayoutRuleStateStoreName,
                    payoutRuleClient = payoutRuleClient,
                    ruleKafkaTemplate = ruleKafkaTemplate,
                ),
            )
            // [스트림 프로세서] 정산 베이스 저장
            .peek({ _, message -> settlementService.saveBase(message) })
        // .print(Printed.toSysOut<String, Base>().withLabel("payment-stream"))

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

        aggregatedTable.toStream() // 집계된 정보를 KStream으로 변환하여 다른 토픽으로 전송
            .to(
                kafkaProperties.paymentStatisticsTopic,
                Produced.with(
                    serdeFactory.baseAggregationKeySerde(),
                    serdeFactory.baseAggregateValueSerde()
                )
            )

        /*************************************
         * 4. 카프카 스트림즈 인스턴스 생성
         */
        return KafkaStreams(builder.build(), streamsConfig)
    }

    private fun applyGlobalTable(builder: StreamsBuilder) {
        builder.globalTable(
            kafkaProperties.paymentRulesGlobalTopic,
            Materialized.`as`<String, Rule, KeyValueStore<Bytes, ByteArray>>(kafkaProperties.globalPayoutRuleStateStoreName)
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