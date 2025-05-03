package kafkastreams.study.sample.settlement

import kafkastreams.study.common.randomEnum
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.common.Type
import kafkastreams.study.sample.settlement.payment.Payment
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

@SpringBootTest
class SettlementKafkaStreamsTest {
    @Autowired
    private lateinit var settlementKafkaStreams: SettlementKafkaStreams

    @Autowired
    private lateinit var paymentKafkaTemplate: KafkaTemplate<String, StreamMessage<Payment>>

    @Test
    fun settlementKafkaStreams() {
        val payment = Payment(
            paymentType = PaymentType.ONLINE,
            amount = Random.nextLong(1000L, 1000000L),
            payoutDate = LocalDate.now().plusDays(2),
            confirmDate = LocalDate.now().plusDays(2),
            merchantNumber = "merchant-${Random.nextInt(1000, 9999)}",
            paymentDate = LocalDateTime.now(),
            paymentActionType = randomEnum<PaymentActionType>(),
            paymentMethodType = randomEnum<PaymentMethodType>(),
        )
        paymentKafkaTemplate.send(
            "test-payment",
            UUID.randomUUID().toString(),
            StreamMessage(
                action = Type.PAYMENT,
                channel = PaymentType.ONLINE,
                data = payment,
            )
        )

        settlementKafkaStreams.settlementStreams()
    }
}