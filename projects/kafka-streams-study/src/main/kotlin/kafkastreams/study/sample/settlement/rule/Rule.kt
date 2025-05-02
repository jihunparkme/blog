package kafkastreams.study.sample.settlement.rule

import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import java.time.LocalDate
import java.time.LocalDateTime

data class Rule(
    val ruleId: String,
    val payoutDate: LocalDate,
    val confirmDate: LocalDate,

    val merchantNumber: String,
    val paymentDate: LocalDateTime,
    val paymentActionType: PaymentActionType,
    val paymentMethodType: PaymentMethodType,
)