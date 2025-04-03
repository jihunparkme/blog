package example.matrix

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

private var streams: KafkaStreams? = null

fun main() {
    Runtime.getRuntime().addShutdownHook(ShutdownThread())

    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "metric-streams-application"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    /** 카프카 스트림즈의 토폴로지 정의를 위한 StreamsBuilder 인스턴스 생성  */
    val builder = StreamsBuilder()
    val metrics = builder.stream<String, String>("metric.all")

    val cpuMetrics = metrics.filter { _, value ->
        MetricJsonUtils.getMetricName(value) == "cpu"
    }
    val memoryMetrics = metrics.filter { _, value ->
        MetricJsonUtils.getMetricName(value) == "memory"
    }

    cpuMetrics.to("metric.cpu")
    memoryMetrics.to("metric.memory")

    /** 분기된 데이터 중 전체 CPU 사용량이 50%가 넘어갈 경우 필터링  */
    val filteredCpuMetric = cpuMetrics
        .filter({ key: String?, value: String? -> MetricJsonUtils.getTotalCpuPercent(value) > 0.5 })

    /** 전체 CPU 사용량의 50%가 넘는 데이터의 host, timestamp 값 조합을 전달  */
    filteredCpuMetric.mapValues({ value: String? ->
        MetricJsonUtils.getHostTimestamp(
            value
        )
    }).to("metric.cpu.alert")

    /** StreamsBuilder 인스턴스로 정의된 토폴로지와 스트림즈 설정값을 토대로 KafkaStreams 인스턴스를 생성하고 실행  */
    streams = KafkaStreams(builder.build(), props)
    streams!!.start()
}

internal class ShutdownThread : Thread() {
    override fun run() {
        /** Kafka Stream의 안전한 종료를 위해 셧다운 훅을 받을 경우 close() 메서드 호출로 안전하게 종료  */
        streams!!.close()
    }
}
