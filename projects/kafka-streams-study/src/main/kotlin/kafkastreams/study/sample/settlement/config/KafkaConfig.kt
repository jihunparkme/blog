package kafkastreams.study.sample.settlement.config

import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
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
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.servers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java, // 기본 값 직렬화기 클래스
        )
    }

    private inline fun <reified V> createProducerFactory(): ProducerFactory<String, V> {
        val keySerializer = StringSerializer().apply {
            configure(producerConfigs(), true) // isKey = true
        }
        val valueSerializer = JsonSerializer<V>(objectMapper)
        return DefaultKafkaProducerFactory(
            producerConfigs(),
            keySerializer,
            valueSerializer
        )
    }

    @Bean
    fun paymentKafkaTemplate(): KafkaTemplate<String, StreamMessage<Payment>> {
        return KafkaTemplate(createProducerFactory())
    }

    @Bean
    fun ruleKafkaTemplate(): KafkaTemplate<String, Rule> {
        return KafkaTemplate(createProducerFactory())
    }
}

@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(
    val servers: String,
    val partition: Int,
    val paymentTopic: String,
    val paymentRulesGlobalTopic: String,
    val paymentApplicationName: String,
    val globalPayoutRuleStateStoreName: String,
    val statisticsStoreName: String,
)