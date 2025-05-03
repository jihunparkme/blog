package kafkastreams.study.sample.settlement.controller

import kafkastreams.study.common.BasicResponse
import kafkastreams.study.common.Result
import kafkastreams.study.sample.settlement.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/api/payment")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @GetMapping
    fun sendToTopic(): ResponseEntity<BasicResponse<Result>> {
        paymentService.sendToTopic()
        return BasicResponse.ok(Result.SUCCESS)
    }
}

