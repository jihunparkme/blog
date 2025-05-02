package kafkastreams.study.sample.settlement.settlement

import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import java.time.LocalDate
import java.time.LocalDateTime

data class Statistics(
    val count: Long,
    val totalAmount: Long,
    val confirmDate: LocalDate,
    val payoutDate: LocalDate,
    val paymentType: PaymentType,

    val merchantNumber: String,
    val paymentDate: LocalDateTime,
    val paymentActionType: PaymentActionType,
    val paymentMethodType: PaymentMethodType,
)