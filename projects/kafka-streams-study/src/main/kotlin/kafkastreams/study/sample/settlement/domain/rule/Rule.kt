package kafkastreams.study.sample.settlement.domain.rule

import kafkastreams.study.sample.settlement.common.DEFAULT_PAYOUT_DATE
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import java.time.LocalDate
import java.time.LocalDateTime

data class Rule(
    val ruleId: String? = null,
    val payoutDate: LocalDate = DEFAULT_PAYOUT_DATE,
    val confirmDate: LocalDate = DEFAULT_PAYOUT_DATE,

    val merchantNumber: String? = null,
    val paymentDate: LocalDateTime = LocalDateTime.now(),
    val paymentActionType: PaymentActionType? = null,
    val paymentMethodType: PaymentMethodType? = null,
)