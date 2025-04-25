package kafkastreams.study.sample.payout.payment

import kafkastreams.study.sample.payout.common.PaymentMethodType
import kafkastreams.study.sample.payout.common.PaymentType
import java.time.LocalDate
import java.time.LocalDateTime

data class Payment(
    val paymentType: PaymentType,
    val amount: Long,
    val paymentDate: LocalDateTime,
    val merchantNumber: String,
    val payoutDate: LocalDate,
    val confirmDate: LocalDate,
    val paymentMethodType: PaymentMethodType,
)
