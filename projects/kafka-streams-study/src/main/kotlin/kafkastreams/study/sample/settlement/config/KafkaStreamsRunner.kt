package kafkastreams.study.sample.settlement.config

import jakarta.annotation.PreDestroy
import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.SettlementKafkaStreams
import org.apache.kafka.streams.KafkaStreams
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class KafkaStreamsRunner (
    private val settlementKafkaStreams: SettlementKafkaStreams,
) : CommandLineRunner {

    private lateinit var settlementStreams: KafkaStreams

    override fun run(vararg args: String?) {
        with(settlementKafkaStreams) {
            log.info("Starting Kafka Streams")
            settlementStreams = settlementStreams()
            settlementStreams.start()
        }
    }

    @PreDestroy
    fun closeStreams() {
        log.info("Closing Kafka Streams")
        settlementStreams.close()
    }

    companion object {
        private val log by logger()
    }
}