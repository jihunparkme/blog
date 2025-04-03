package kafkastreams.study

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaStreamsStudyApplication

fun main(args: Array<String>) {
    runApplication<KafkaStreamsStudyApplication>(*args)
}
