package kafkastreams.study.sample.settlement.controller

import kafkastreams.study.sample.settlement.common.PaymentActionType
import kafkastreams.study.sample.settlement.common.PaymentMethodType
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregateValue
import kafkastreams.study.sample.settlement.domain.aggregation.BaseAggregationKey
import kafkastreams.study.sample.settlement.service.AggregatedDataEntry
import kafkastreams.study.sample.settlement.service.AggregationQueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/aggregation-data")
class AggregationQueryController(private val aggregationQueryService: AggregationQueryService) {
    @GetMapping
    fun getData(
        @RequestParam merchantNumber: String,
        @RequestParam paymentDateDailyStr: String,
        @RequestParam actionTypeStr: String,
        @RequestParam methodTypeStr: String
    ): ResponseEntity<BaseAggregateValue> {
        val key = BaseAggregationKey(
            merchantNumber = merchantNumber,
            paymentDateDaily = LocalDate.parse(paymentDateDailyStr),
            paymentActionType = PaymentActionType.valueOf(actionTypeStr.uppercase()),
            paymentMethodType = PaymentMethodType.valueOf(methodTypeStr.uppercase())
        )
        val result = aggregationQueryService.getAggregatedValue(key)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/all")
    fun getAllData(): ResponseEntity<List<AggregatedDataEntry>> {
        val allData = aggregationQueryService.getAllAggregatedValues()
        return ResponseEntity.ok(allData)
    }
}