package kafkastreams.study.sample.settlement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregateValue
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregationKey
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
import kafkastreams.study.sample.settlement.domain.settlement.Base
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Component
class SerdeFactory(
    private val objectMapper: ObjectMapper,
) {
    fun messagePaymentSerde(): JsonSerde<StreamMessage<Payment>> {
        val streamMessagePaymentDeserializer = JsonDeserializer(
            object : TypeReference<StreamMessage<Payment>>() {},
            objectMapper,
            false // failOnUnknownProperties
        )
        streamMessagePaymentDeserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.common.*",
            "kafkastreams.study.sample.settlement.domain.*",
        )

        return JsonSerde(
            JsonSerializer(objectMapper),
            streamMessagePaymentDeserializer
        )
    }

    fun baseSerde(): JsonSerde<Base> {
        val streamMessagePaymentDeserializer = JsonDeserializer(
            object : TypeReference<Base>() {},
            objectMapper,
            false
        )
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
            false
        ) // Kafka 메시지를 역직렬화할 때 메시지 헤더에 있는 타입 정보를 사용할지 여부
        streamMessagePaymentDeserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.*",
        )

        return JsonSerde(
            JsonSerializer(objectMapper),
            streamMessagePaymentDeserializer
        )
    }

    fun baseAggregationKeySerde(): JsonSerde<BaseAggregationKey> {
        val deserializer = JsonDeserializer(
            object : TypeReference<BaseAggregationKey>() {},
            objectMapper,
            false
        )
        deserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.aggregation.*",
            "kafkastreams.study.sample.settlement.common.*"
        )
        return JsonSerde(JsonSerializer(objectMapper), deserializer)
    }

    fun baseAggregateValueSerde(): JsonSerde<BaseAggregateValue> {
        val deserializer = JsonDeserializer(
            object : TypeReference<BaseAggregateValue>() {},
            objectMapper,
            false
        )
        deserializer.addTrustedPackages(
            "kafkastreams.study.sample.settlement.domain.aggregation.*"
        )
        return JsonSerde(JsonSerializer(objectMapper), deserializer)
    }
}