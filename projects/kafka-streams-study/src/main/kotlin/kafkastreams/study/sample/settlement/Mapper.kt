package kafkastreams.study.sample.settlement

import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.settlement.Base
import org.apache.kafka.streams.kstream.ValueMapper

class BaseMapper() : ValueMapper<Payment, Base> {
    override fun apply(payment: Payment): Base? {
        return Base(
            paymentType = payment.paymentType ?: throw IllegalArgumentException(),
            amount = payment.amount,
            payoutDate = payment.payoutDate,
            confirmDate = payment.confirmDate,
            merchantNumber = payment.merchantNumber ?: throw IllegalArgumentException(),
            paymentDate = payment.paymentDate,
            paymentActionType = payment.paymentActionType ?: throw IllegalArgumentException(),
            paymentMethodType = payment.paymentMethodType ?: throw IllegalArgumentException(),
        )
    }
}