package kafkastreams.study.sample.settlement.config

import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.payment.Payment
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
class PaymentKafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun paymentProducerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.servers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        )
    }

    @Bean
    fun paymentProducerFactory(): ProducerFactory<String, StreamMessage<Payment>> {
        return DefaultKafkaProducerFactory(paymentProducerConfigs())
    }

    @Bean
    fun paymentKafkaTemplate(
        producerFactory: ProducerFactory<String, StreamMessage<Payment>>
    ): KafkaTemplate<String, StreamMessage<Payment>> {
        return KafkaTemplate(producerFactory)
    }
}

@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(
    val servers: String,
    val partition: Int,
    val paymentTopic: String,
)