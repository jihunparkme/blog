package kafkastreams.study.sample.settlement.service

import kafkastreams.study.common.randomEnum
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.common.Type
import kafkastreams.study.sample.settlement.config.KafkaProperties
import kafkastreams.study.sample.settlement.payment.Payment
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

@Service
class PaymentService(
    private val paymentKafkaTemplate: KafkaTemplate<String, StreamMessage<Payment>>,
    private val kafkaProperties: KafkaProperties,
) {
    fun sendToTopic(count: Int) {
        val paymentType = randomEnum<PaymentType>()
        sendPayment(count, paymentType)
        sendFinishMessage(paymentType)
    }

    private fun sendPayment(count: Int, paymentType: PaymentType) {
        repeat(count) {
            val payment = Payment(
                paymentType = paymentType,
                amount = Random.nextLong(1000L, 1000000L),
                payoutDate = LocalDate.now().plusDays(2),
                confirmDate = LocalDate.now().plusDays(2),
                merchantNumber = "merchant-${Random.nextInt(1000, 9999)}",
                paymentDate = LocalDateTime.now(),
                paymentActionType = randomEnum<PaymentActionType>(),
                paymentMethodType = randomEnum<PaymentMethodType>(),
            )
            paymentKafkaTemplate.send(
                kafkaProperties.paymentTopic,
                UUID.randomUUID().toString(),
                StreamMessage(
                    action = Type.PAYMENT,
                    channel = paymentType,
                    data = payment,
                )
            )
        }
    }

    private fun sendFinishMessage(paymentType: PaymentType) {
        repeat(kafkaProperties.partition) {
            paymentKafkaTemplate.send(
                kafkaProperties.paymentTopic,
                it,
                UUID.randomUUID().toString(),
                StreamMessage(
                    action = Type.FINISH,
                    channel = paymentType,
                )
            )
        }
    }
}
