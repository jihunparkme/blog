package kafkastreams.study.sample.settlement.common

import java.time.LocalDate

val DEFAULT_PAYOUT_DATE = LocalDate.now().plusDays(2)

data class StreamMessage<T>(
    val channel: PaymentType? = null,
    val action: Type? = null,
    val data: T? = null,
)