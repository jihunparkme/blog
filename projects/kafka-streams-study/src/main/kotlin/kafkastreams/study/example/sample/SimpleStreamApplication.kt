package kafkastreams.study.example.sample

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

/**
 * 애플리케이션 아이디 값 기준으로 병렬처리 수행
 * - 다른 스트림즈 애플리케이션을 운영한다면 다른 아이디를 사용
 */
private const val APPLICATION_NAME = "streams-application"
/** 스트림즈 애플리케이션과 연동할 카프카 클러스터 정보  */
private const val BOOTSTRAP_SERVERS = "localhost:9092"
private const val STREAM_LOG = "stream_log"
private const val STREAM_LOG_COPY = "stream_log_copy"

fun main() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = APPLICATION_NAME
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    /** 스트림 처리를 위해 메시지 키/값의 역직렬화, 직렬화 방식 지정  */
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    /** 스트림 토폴로지를 정의하기 위한 용도  */
    val builder = StreamsBuilder()

    /**
     * 최초의 토픽 데이터를 가져오는 소스 프로세서
     * - KStream 생성 -> stream()
     * - KTable 생성 -> table()
     * - GlobalKTable 생성 -> globalTable()
     */
    val stream = builder.stream<String, String>(STREAM_LOG)
    stream.foreach { k: String, v: String ->
        println(
            "$k: $v"
        )
    }

    /**
     * 싱크 프로세서
     * - 토픽을 담은 KStream 객체를 다른 토픽으로 전송하기 위한 to()
     */
    stream.to(STREAM_LOG_COPY)

    /**
     * StreamsBuilder로 정의한 토폴로이제 대한 정보와 스트림즈 실행을 위한 기본 옵션을 파라미터로 KafkaStreams 인스턴스 생성
     * 토픽(stream_log)의 데이터를 다른 토픽(stream_log_copy)으로 전달
     */
    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
