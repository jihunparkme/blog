package kafkastreams.study.sample.settlement

import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.client.PayoutDateRequest
import kafkastreams.study.sample.settlement.client.PayoutRuleClient
import kafkastreams.study.sample.settlement.domain.rule.Rule
import kafkastreams.study.sample.settlement.domain.settlement.Base
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.state.KeyValueStore

class PayoutRuleProcessValues(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient,
) : FixedKeyProcessorSupplier<String, Base, Base> {
    override fun get(): FixedKeyProcessor<String, Base, Base> {
        return PayoutRuleProcessor(stateStoreName, payoutRuleClient)
    }
}

class PayoutRuleProcessor(
    private val stateStoreName: String,
    private val payoutRuleClient: PayoutRuleClient
) : FixedKeyProcessor<String, Base, Base> {
    private var context: FixedKeyProcessorContext<String, Base>? = null
    private var payoutRuleStore: KeyValueStore<String, Rule>? = null

    override fun init(context: FixedKeyProcessorContext<String, Base>) {
        this.context = context
        this.payoutRuleStore = this.context?.getStateStore(stateStoreName)
    }

    override fun process(record: FixedKeyRecord<String, Base>) {
        val key = record.key()
        val base = record.value()

        // 결제 데이터가 없을 경우 스킵
        if (base == null) {
            log.info(">>> [결제 데이터 누락] Payment data is null, skipping processing for key: $key")
            return
        }

        // stateStore에 저장된 지급룰 조회
        val ruleKey = "${base.merchantNumber}/${base.paymentDate.toLocalDate()}/${base.paymentActionType}/${base.paymentMethodType}"
        var rule = payoutRuleStore?.get(ruleKey)
        // stateStore에 지급룰이 저장되어 있지 않을 경우 API 요청 후 저장
        if (rule == null) {
            log.info(">>> [지급룰 조회] Search payout rule.. $key")
            val findRule = payoutRuleClient.getPayoutDate(
                PayoutDateRequest(
                    merchantNumber = base.merchantNumber ?: throw IllegalArgumentException(),
                    paymentDate = base.paymentDate,
                    paymentActionType = base.paymentActionType ?: throw IllegalArgumentException(),
                    paymentMethodType = base.paymentMethodType ?: throw IllegalArgumentException(),
                )
            )
            payoutRuleStore?.put(ruleKey, findRule)
            rule = findRule
        }

        // 가맹점에 대한 지급룰이 없을 경우
        if (rule == null) {
            log.info(">>> [지급룰 없음] Not found payment payout rule. key: $key")
            base.updateDefaultPayoutDate()
        }

        // 지급룰 업데이트 대상일 경우
        if (rule != null && (rule.payoutDate != base.payoutDate || rule.confirmDate != base.confirmDate)) {
            log.info(">>> [지급룰 정보 저장] Save payout date.. $key")
            base.updatePayoutDate(rule)
        }

        context?.forward(record.withValue(base))
    }

    override fun close() {
        this.close()
    }

    companion object {
        private val log by logger()
    }
}