package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.config.KafkaProperties
import kafkastreams.study.sample.settlement.config.KafkaStreamsConfig
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.service.SettlementService
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.kstream.WindowedSerdes
import org.apache.kafka.streams.state.WindowStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonSerde
import java.time.Duration

@Configuration
class SettlementKafkaStreamsApp(
    private val streamsConfig: KafkaStreamsConfig,
    private val kafkaProperties: KafkaProperties,
    private val settlementService: SettlementService,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun settlementStreams(): KafkaStreams {
        val builder = StreamsBuilder()

        // [소스 프로세서] 결제 토픽으로부터 결제 데이터 받기
        val stringSerde = Serdes.String()
        val paymentSerde = JsonSerde(object : TypeReference<Payment>() {}, objectMapper)
        val aggregatedStatsSerde = JsonSerde<AggregatedPaymentStats>()
        val paymentStream = builder.stream(
            kafkaProperties.paymentTopic,
            Consumed.with(
                stringSerde,
                paymentSerde
            )
        )
        println("ddddddasfdasfasfdasfdsafs")
        paymentStream
            // [스트림 프로세서] 결제 메시지 로그 저장
            // .peek({ _, message -> settlementService.savePaymentMessageLog(message) })
            .selectKey { _, message -> message.merchantNumber }
            .print(Printed.toSysOut<String, Payment>().withLabel("payment-stream"))

        val reKeyedForGlobalAggregation: KStream<String, Payment> = paymentStream
            .selectKey { _, _ -> "GLOBAL_AGG_KEY" }

        val initializer = Initializer { AggregatedPaymentStats() }
        val aggregator = Aggregator { _: String, payment: Payment, aggregate: AggregatedPaymentStats ->
            aggregate.update(payment.amount)
        }

        val dailyAggregations: KTable<Windowed<String>, AggregatedPaymentStats> = reKeyedForGlobalAggregation
            .groupByKey(Grouped.with(stringSerde, paymentSerde))
            // .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(1))) // Grace Period 없음
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10))) // Grace Period 없음
            .aggregate(
                initializer,
                aggregator,
                Materialized.`as`<String, AggregatedPaymentStats, WindowStore<Bytes, ByteArray>>(
                    "daily-payment-aggregations-store" // 상태 저장소 이름
                )
                    .withKeySerde(stringSerde)
                    .withValueSerde(aggregatedStatsSerde)
            )

        dailyAggregations.toStream()
            .mapValues { readOnlyWindowedKey, stats ->
                // 필요시 WindowedKey 에서 실제 윈도우 정보 추출하여 함께 전송
                print("Start: ${readOnlyWindowedKey.window().startTime()}, End: ${readOnlyWindowedKey.window().endTime()}, Stats: $stats")
                "Start: ${readOnlyWindowedKey.window().startTime()}, End: ${readOnlyWindowedKey.window().endTime()}, Stats: $stats"
            }
            // .to("daily-aggregated-payments-topic", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String::class.java, Duration.ofDays(1).toMillis()), stringSerde))
            .to("daily-aggregated-payments-topic", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String::class.java, Duration.ofSeconds(10).toMillis()), stringSerde))

        /**
         * [스트림 프로세서]
         * - 룰 조회(ktable 활용 룰 관리)
         *
         * [스트림 프로세서]
         * - 베이스 생성 및 저장
         *
         * [스트림 프로세서]
         * - 결제 데이터 집계(Group By key)
         *
         * [스트림 프로세서]
         * - 각 파티션에 FINISH 메시지가 도착하면 집계로 된 일통계 저장
         * ...
         *
         * GlobalKTable(활용?) 환율?
         *
         * 스트림 과정을 printed 로 확인
         */
        return KafkaStreams(builder.build(), streamsConfig.properties(kafkaProperties.paymentApplicationName))
    }
}

data class AggregatedPaymentStats(
    var totalAmount: Double = 0.0,
    var transactionCount: Long = 0L
) {
    // 집계를 위한 업데이트 함수
    fun update(paymentAmount: Long): AggregatedPaymentStats {
        this.totalAmount += paymentAmount
        this.transactionCount++
        return this
    }
}