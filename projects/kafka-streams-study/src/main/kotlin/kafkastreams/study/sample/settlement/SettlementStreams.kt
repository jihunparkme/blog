package kafkastreams.study.sample.settlement

import kafkastreams.study.sample.settlement.payment.Payment
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

private const val APPLICATION_NAME = "settlement-streams"
private const val BOOTSTRAP_SERVERS = "localhost:9092"
private const val PAYMENT_TOPIC = "payment"

fun main() {
    /**
     * [소스 프로세서]
     * - 결제 데이터 받기
     * - 결제 로그 저장
     *
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
    val props = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME)
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
    }

    val builder = StreamsBuilder()

    /**
     * [소스 프로세서]
     * - 결제 데이터 받기
     * - 결제 로그 저장
     */
    val paymentStream = builder.stream<String, Payment>(PAYMENT_TOPIC)
    paymentStream.foreach { k: String, v: Payment ->
        println(
            "$k: $v"
        )
    }
}
