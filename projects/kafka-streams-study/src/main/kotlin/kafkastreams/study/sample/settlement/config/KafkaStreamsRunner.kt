package kafkastreams.study.sample.settlement.config

import jakarta.annotation.PreDestroy
import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.SettlementKafkaStreamsApp
import org.apache.kafka.streams.KafkaStreams
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class KafkaStreamsRunner(
    private val settlementKafkaStreamsApp: SettlementKafkaStreamsApp,
) : CommandLineRunner {

    private lateinit var settlementStreams: KafkaStreams

    override fun run(vararg args: String?) {
        settlementStreams = settlementKafkaStreamsApp.settlementStreams()
        val currentState = settlementStreams.state()
        if (currentState == KafkaStreams.State.CREATED ||
            currentState == KafkaStreams.State.NOT_RUNNING
        ) {
            settlementStreams.start()
            log.info("Kafka Streams started.")
            return
        }

        log.warn("Kafka Streams is already running or in an unexpected state: {}. Not starting again.", currentState)
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