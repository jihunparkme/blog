package kafkastreams.study.sample.settlement.processor

import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.client.PayoutDateRequest
import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.rule.Rule
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.state.KeyValueStore

class PayoutRuleProcessValues(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient,
) : FixedKeyProcessorSupplier<String, StreamMessage<Payment>, Payment> {
    override fun get(): FixedKeyProcessor<String, StreamMessage<Payment>, Payment> {
        return PayoutRuleProcessor(stateStoreName, payoutRuleClient)
    }
}

class PayoutRuleProcessor(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient
) : FixedKeyProcessor<String, StreamMessage<Payment>, Payment> {
    private var context: FixedKeyProcessorContext<String, Payment>? = null
    private var payoutRuleStore: KeyValueStore<String, Rule>? = null

    override fun init(context: FixedKeyProcessorContext<String, Payment>) {
        this.context = context
        this.payoutRuleStore = this.context?.getStateStore(stateStoreName)
    }

    override fun process(record: FixedKeyRecord<String, StreamMessage<Payment>>) {
        val key = record.key()
        val payment = record.value().data

        // 결제 데이터가 없을 경우 스킵
        if (payment == null) {
            log.info(">>> Payment data is null, skipping processing for key: $key")
            return
        }

        // stateStore에 저장된 지급룰 조회
        var rule = payoutRuleStore?.get(stateStoreName)
        // stateStore에 지급룰이 저장되어 있지 않을 경우 API 요청 후 저장
        if (rule == null) {
            log.info(">>> 🔎🔎🔎 Search payout rule.. $key")
            val findRule = payoutRuleClient.getPayoutDate(
                PayoutDateRequest(
                    merchantNumber = payment.merchantNumber,
                    paymentDate = payment.paymentDate,
                    paymentActionType = payment.paymentActionType,
                    paymentMethodType = payment.paymentMethodType
                )
            )
            payoutRuleStore?.put(stateStoreName, findRule)
            rule = findRule
        }

        // 가맹점에 대한 지급룰이 없을 경우
        if (rule == null) {
            log.info(">>> Not found payment payout rule. key: $key")
            payment.updateDefaultPayoutDate()
        }

        // 지급룰 업데이트 대상일 경우
        if (rule != null && (rule.payoutDate != payment.payoutDate || rule.confirmDate != payment.confirmDate)) {
            log.info(">>> 📦📦📦 Save payout date.. $key")
            payment.updatePayoutDate(rule)
        }

        context?.forward(record.withValue(payment))
    }

    override fun close() {
        this.close()
    }

    companion object {
        private val log by logger()
    }
}