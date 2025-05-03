package kafkastreams.study.sample.settlement.controller

import kafkastreams.study.common.BasicResponse
import kafkastreams.study.common.Result
import kafkastreams.study.sample.settlement.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @GetMapping("/send/{count}")
    fun sendToTopic(
        @PathVariable count: Int = 100
    ): ResponseEntity<BasicResponse<Result>> {
        paymentService.sendToTopic(count)
        return BasicResponse.ok(Result.SUCCESS)
    }
}

