package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.config.KafkaStreamsConfig
import kafkastreams.study.sample.settlement.payment.Payment
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val APPLICATION_NAME = "settlement-streams"
private const val PAYMENT_TOPIC = "payment"

@Configuration
class SettlementKafkaStreams(
    private val objectMapper: ObjectMapper,
    private val streamsConfig: KafkaStreamsConfig
) {
    @Bean
    fun settlementStreams(): KafkaStreams {
        val builder = StreamsBuilder()
        /**
         * [소스 프로세서]
         * - 결제 데이터 받기
         * - 결제 로그 저장
         */
        val paymentStream = builder.stream<String, Payment>(PAYMENT_TOPIC)

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
        return KafkaStreams(builder.build(), streamsConfig.properties(APPLICATION_NAME))
    }
}