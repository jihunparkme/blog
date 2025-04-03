package study.ktable

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

private const val APPLICATION_NAME = "order-join-application"
private const val BOOTSTRAP_SERVERS = "localhost:9092"
private const val ADDRESS_TABLE = "address"
private const val ORDER_STREAM = "order"
private const val ORDER_JOIN_STREAM = "order_join"

fun main() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = APPLICATION_NAME
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()

    /** 소스 프로세서 */
    val addressTable = builder.table<String, String>(ADDRESS_TABLE)
    val orderStream = builder.stream<String, String>(ORDER_STREAM)

    /** 스트림 프로세서 */
    orderStream.join(addressTable) { // join()을 수행할 KTable 인스턴스
        // KStream, KTable 에서 동일한 메시지 키를 가진 데이터 발견 경우 각각의 메시지 값을 조합해서 만들 데이터 정의
            order: String, address: String ->
        println("$order send to $address")
        "$order send to $address"
    }
        /** 싱크 프로세서  */
        .to(ORDER_JOIN_STREAM)

    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
