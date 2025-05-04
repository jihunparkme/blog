package kafkastreams.study.sample.settlement.domain.payment

import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import java.time.LocalDate
import java.time.LocalDateTime

data class Payment(
    val paymentType: PaymentType,
    val amount: Long,
    val payoutDate: LocalDate,
    val confirmDate: LocalDate,

    val merchantNumber: String,
    val paymentDate: LocalDateTime,
    val paymentActionType: PaymentActionType,
    val paymentMethodType: PaymentMethodType,
)
