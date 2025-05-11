package kafkastreams.study.sample.settlement.domain.settlement

import kafkastreams.study.sample.settlement.common.DEFAULT_PAYOUT_DATE
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import kafkastreams.study.sample.settlement.domain.rule.Rule
import java.time.LocalDate
import java.time.LocalDateTime

data class Base(
    val paymentType: PaymentType,
    val amount: Long,
    var payoutDate: LocalDate,
    var confirmDate: LocalDate,

    val merchantNumber: String,
    val paymentDate: LocalDateTime,
    val paymentActionType: PaymentActionType,
    val paymentMethodType: PaymentMethodType,
) {
    fun updatePayoutDate(rule: Rule) {
        this.payoutDate = rule.payoutDate
        this.confirmDate = rule.confirmDate
    }

    fun updateDefaultPayoutDate() {
        this.payoutDate = DEFAULT_PAYOUT_DATE
        this.confirmDate = DEFAULT_PAYOUT_DATE
    }
}