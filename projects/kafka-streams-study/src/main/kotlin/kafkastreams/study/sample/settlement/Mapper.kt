package kafkastreams.study.sample.settlement

import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.settlement.Base
import org.apache.kafka.streams.kstream.ValueMapper

class BaseMapper() : ValueMapper<StreamMessage<Payment>, Base> {
    override fun apply(payment: StreamMessage<Payment>): Base {
        return Base(
            paymentType = payment.data?.paymentType ?: throw IllegalArgumentException(),
            amount = payment.data.amount,
            payoutDate = payment.data.payoutDate,
            confirmDate = payment.data.confirmDate,
            merchantNumber = payment.data.merchantNumber ?: throw IllegalArgumentException(),
            paymentDate = payment.data.paymentDate,
            paymentActionType = payment.data.paymentActionType ?: throw IllegalArgumentException(),
            paymentMethodType = payment.data.paymentMethodType ?: throw IllegalArgumentException(),
        )
    }
}