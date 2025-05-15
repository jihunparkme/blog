package kafkastreams.study.sample.settlement

import kafkastreams.study.common.randomEnum
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.common.Type
import kafkastreams.study.sample.settlement.config.KafkaStreamsRunner
import kafkastreams.study.sample.settlement.domain.payment.Payment
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettlementKafkaStreamsTest {
    @Autowired
    private lateinit var paymentKafkaTemplate: KafkaTemplate<String, StreamMessage<Payment>>

    @Autowired
    private lateinit var KafkaStreamsRunner: KafkaStreamsRunner

    @BeforeAll
    fun beforeAll() {
        KafkaStreamsRunner.run()
    }

    @AfterAll
    fun afterAll() {
        KafkaStreamsRunner.closeStreams()
    }

    @Test
    fun integration() {
        Thread.sleep(3000)
        val payment = testPayment()
        paymentKafkaTemplate.send(
            "test-payment",
            UUID.randomUUID().toString(),
            StreamMessage(
                action = Type.PAYMENT,
                channel = PaymentType.ONLINE,
                data = payment,
            )
        ).get(10, TimeUnit.SECONDS)
        Thread.sleep(5000)
    }

    @Test
    fun ruleStateStore() {
        val payment = testPayment()
        paymentKafkaTemplate.send(
            "test-payment",
            UUID.randomUUID().toString(),
            StreamMessage(
                action = Type.PAYMENT,
                channel = PaymentType.ONLINE,
                data = payment,
            )
        ).get(10, TimeUnit.SECONDS)

        paymentKafkaTemplate.send(
            "test-payment",
            UUID.randomUUID().toString(),
            StreamMessage(
                action = Type.PAYMENT,
                channel = PaymentType.ONLINE,
                data = payment,
            )
        ).get(10, TimeUnit.SECONDS)
    }
}

private fun testPayment(): Payment {
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
    return payment
}