package study.globalKTable

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

private const val APPLICATION_NAME = "global-table-join-application"
private const val BOOTSTRAP_SERVERS = "localhos t:9092"
private const val ADDRESS_GLOBAL_TABLE = "address_v2"
private const val ORDER_STREAM = "order"
private const val ORDER_JOIN_STREAM = "order_join"

fun main() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = APPLICATION_NAME
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()

    /**
     * 소스 프로세서
     * - address_v2 Topic: GlobalKTable 정
     * - order Topic: KStream 생성
     */
    val addressGlobalTable = builder.globalTable<String, String>(ADDRESS_GLOBAL_TABLE)
    val orderStream = builder.stream<String, String>(ORDER_STREAM)

    /**
     * 스트림 프로세서
     * - 조인을 위해 KStream 에 정의된 join() 사용
     */
    orderStream.join(
        addressGlobalTable,  // 조인을 수행할 GlobalKTable 인스턴스
        // GlobalKTable 은 KTable 조인과 다르게 레코드를 매칭할 때
        // KStream 의 메시지 키와 메시지 값 둘 다 사용 가능
        { orderKey: String, orderValue: String? -> orderKey },  // 주문 물품과 주소를 조합하여 String 타입으로 생성
        { order: String, address: String -> "$order send to $address" })
        /**
         * 싱크 프로세서
         * - 조인을 통해 생성된 데이터를 토픽에 저장
         */
        .to(ORDER_JOIN_STREAM)
    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
