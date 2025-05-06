package kafkastreams.study.sample.settlement

import kafkastreams.study.common.randomEnum
import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.common.PaymentType
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.common.Type
import kafkastreams.study.sample.settlement.config.KafkaStreamsRunner
import kafkastreams.study.sample.settlement.domain.payment.Payment
import org.apache.kafka.streams.KafkaStreams
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettlementKafkaStreamsTest {
    @Autowired
    private lateinit var kafkaStreamsRunner: KafkaStreamsRunner

    @Autowired
    private lateinit var settlementKafkaStreamsApp: SettlementKafkaStreamsApp

    @Autowired
    private lateinit var paymentKafkaTemplate: KafkaTemplate<String, StreamMessage<Payment>>

    private lateinit var settlementStreams: KafkaStreams

    @BeforeAll
    fun beforeAll() {
        kafkaStreamsRunner.run()
        settlementStreams = settlementKafkaStreamsApp.settlementStreams()
    }

    @AfterAll
    fun afterAll() {
        kafkaStreamsRunner.closeStreams()
    }

    @Test
    fun settlementKafkaStreams() {
        val maxWaitTime = 30.seconds // 최대 대기 시간 (Kotlin Duration 사용)
        val pollIntervalMs = 500L     // 확인 간격 (밀리초)
        val startTime = TimeSource.Monotonic.markNow() // 시작 시간 기록

        // TODO:: org.apache.kafka.streams.errors.StreamsException: ClassCastException invoking processor: KSTREAM-PEEK-0000000002. Do the Processor's input types match the deserialized types? Check the Serde setup and change the default Serdes in StreamConfig or provide correct Serdes via method parameters. Make sure the Processor can accept the deserialized input of type key: java.lang.String, and value: java.lang.String.
        // Note that although incorrect Serdes are a common cause of error, the cast exception might have another cause (in user code, for example). For example, if a processor wires in a store, but casts the generics incorrectly, a class cast exception could be raised during processing, but the cause would not be wrong Serdes.
        // TODO: TEST container 적용해보기

        println("Waiting for Kafka Streams to become RUNNING (manual check)...")
        // while (settlementStreams.state() != KafkaStreams.State.RUNNING) {
        // 타임아웃 확인
        if (startTime.elapsedNow() > maxWaitTime) {
            fail("Kafka Streams did not reach RUNNING state within ${maxWaitTime.inWholeSeconds} seconds. Current state: ${settlementStreams.state()}")
        }

        // 잠시 대기 후 다시 확인
        try {
            Thread.sleep(pollIntervalMs)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // 인터럽트 처리
            fail("Test interrupted while waiting for Kafka Streams state.", e)
        }
        print(".") // 진행 상황 표시 (선택적)
        // }
        println("\nKafka Streams is RUNNING. Current state: ${settlementStreams.state()}")

        // (선택적) RUNNING 상태 단언
        Assertions.assertEquals(
            KafkaStreams.State.RUNNING,
            settlementStreams.state(),
            "KafkaStreams should be in RUNNING state"
        )

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

        settlementKafkaStreamsApp.settlementStreams()
    }
}