package kafkastreams.study.sample.payout.rule

import kafkastreams.study.sample.payout.common.PaymentMethodType
import java.time.LocalDate
import java.time.LocalDateTime

data class Rule(
    val ruleId: String,
    val payoutDate: LocalDate,
    val confirmDate: LocalDate,
    val paymentDate: LocalDateTime,
    val merchantNumber: String,
    val paymentMethodType: PaymentMethodType,
)