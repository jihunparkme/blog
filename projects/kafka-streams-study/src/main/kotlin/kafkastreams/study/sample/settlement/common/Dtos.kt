package kafkastreams.study.sample.settlement.common

data class StreamMessage<T>(
    val type: Type,
    val data: T
)