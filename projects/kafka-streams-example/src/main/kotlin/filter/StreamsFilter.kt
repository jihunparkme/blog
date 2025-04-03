package filter

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

private const val APPLICATION_NAME = "streams-filter-application"
private const val BOOTSTRAP_SERVERS = "localhost:9092"
private const val STREAM_LOG = "stream_log"
private const val STREAM_LOG_FILTER = "stream_log_filter"

fun main() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = APPLICATION_NAME
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()

    /** 소스 프로세서  */
    val streamLog = builder.stream<String, String>(STREAM_LOG)

    /** 스트림 프로세서  */
    val filteredStream = streamLog.filter { key: String?, value: String -> value.length > 5 }
    filteredStream.foreach { k: String, v: String ->
        println(
            "$k: $v"
        )
    }

    /** 싱크 프로세서  */
    filteredStream.to(STREAM_LOG_FILTER)
    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
