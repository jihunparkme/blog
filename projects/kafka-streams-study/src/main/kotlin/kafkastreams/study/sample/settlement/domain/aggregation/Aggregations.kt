package kafkastreams.study.sample.settlement.domain.aggregation

import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import java.time.LocalDate

data class BaseAggregationKey(
    val merchantNumber: String? = null,
    val paymentDateDaily: LocalDate? = null,
    val paymentActionType: PaymentActionType? = null,
    val paymentMethodType: PaymentMethodType? = null
)

data class BaseAggregateValue(
    val totalAmount: Long = 0L,
    val count: Long = 0L
) {
    fun updateWith(amount: Long): BaseAggregateValue {
        return this.copy(
            totalAmount = this.totalAmount + amount,
            count = this.count + 1
        )
    }
}