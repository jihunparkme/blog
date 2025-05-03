package kafkastreams.study.sample.settlement.common

data class StreamMessage<T>(
    val channel: PaymentType,
    val action: Type,
    val data: T? = null,
)