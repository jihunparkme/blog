package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.kafka.support.serializer.JsonSerializer

private val objectMapper: ObjectMapper = ObjectMapper()

fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
    val streamMessagePaymentDeserializer = JsonDeserializer(
        object : TypeReference<StreamMessage<Payment>>() {},
        objectMapper,
        false) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
    streamMessagePaymentDeserializer.addTrustedPackages(
        "kafkastreams.study.sample.settlement.common.*",
        "kafkastreams.study.sample.settlement.domain.*",
    )

    return JsonSerde(
        JsonSerializer(objectMapper),
        streamMessagePaymentDeserializer
    )
}

fun ruleSerde(): JsonSerde<Rule> {
    val streamMessagePaymentDeserializer = JsonDeserializer(
        object : TypeReference<Rule>() {},
        objectMapper,
        false) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
    streamMessagePaymentDeserializer.addTrustedPackages(
        "kafkastreams.study.sample.settlement.domain.*",
    )

    return JsonSerde(
        JsonSerializer(objectMapper),
        streamMessagePaymentDeserializer
    )
}