package kafkastreams.study.sample.settlement.service

import kafkastreams.study.common.logger
import kafkastreams.study.sample.settlement.common.StreamMessage
import kafkastreams.study.sample.settlement.domain.payment.Payment
import kafkastreams.study.sample.settlement.domain.settlement.Base
import org.springframework.stereotype.Service
import kotlin.getValue

@Service
class SettlementService {
    fun savePaymentMessageLog(data: StreamMessage<Payment>) {
        log.info(">>> 📦📦📦 Save payment message log to payment_log.. $data")
    }

    fun saveBase(data: Base) {
        log.info(">>> 📦📦📦 Save base to base.. $data")
    }

    companion object {
        private val log by logger()
    }
}