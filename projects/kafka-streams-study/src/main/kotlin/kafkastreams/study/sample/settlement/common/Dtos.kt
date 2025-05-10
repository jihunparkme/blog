package kafkastreams.study.sample.settlement.common

data class StreamMessage<T>(
    val channel: PaymentType? = null,
    val action: Type? = null,
    val data: T? = null,
)