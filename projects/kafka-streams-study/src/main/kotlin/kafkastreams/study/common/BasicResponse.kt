package kafkastreams.study.common

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

class BasicResponse<T>(
    val status: Int = 0,
    val httpStatusCode: HttpStatusCode,
    val success: Boolean = false,
    val message: String = "",
    val count: Int = 0,
    val data: T? = null,
) {

    companion object {
        fun internalServerError(message: String): ResponseEntity<BasicResponse<Any>> {
            val basicResponse = BasicResponse<Any>(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
                success = false,
                message = message,
            )
            return ResponseEntity(basicResponse, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        fun clientError(message: String): ResponseEntity<BasicResponse<Any>> {
            val basicResponse = BasicResponse<Any>(
                status = HttpStatus.BAD_REQUEST.value(),
                httpStatusCode = HttpStatus.BAD_REQUEST,
                success = false,
                message = message,
            )
            return ResponseEntity(basicResponse, HttpStatus.BAD_REQUEST)
        }

        fun notFound(message: String): ResponseEntity<BasicResponse<Any>> {
            val basicResponse = BasicResponse<Any>(
                status = HttpStatus.NOT_FOUND.value(),
                httpStatusCode = HttpStatus.NOT_FOUND,
                success = false,
                message = message,
            )
            return ResponseEntity(basicResponse, HttpStatus.NOT_FOUND)
        }

        fun ok(data: Boolean): ResponseEntity<BasicResponse<Boolean>> {
            val basicResponse = BasicResponse(
                status = HttpStatus.OK.value(),
                httpStatusCode = HttpStatus.OK,
                success = true,
                data = data,
            )
            return ResponseEntity.ok(basicResponse)
        }

        fun <T> ok(data: T): ResponseEntity<BasicResponse<T>> {
            val basicResponse = BasicResponse(
                status = HttpStatus.OK.value(),
                httpStatusCode = HttpStatus.OK,
                success = true,
                count = 1,
                data = data,
            )
            return ResponseEntity.ok(basicResponse)
        }

        fun <T> ok(data: List<T>): ResponseEntity<BasicResponse<List<T>>> {
            val basicResponse = BasicResponse(
                status = HttpStatus.OK.value(),
                httpStatusCode = HttpStatus.OK,
                success = true,
                count = data.size,
                data = data,
            )
            return ResponseEntity.ok(basicResponse)
        }

        fun <T> created(data: T): ResponseEntity<BasicResponse<T>> {
            val basicResponse = BasicResponse(
                status = HttpStatus.CREATED.value(),
                httpStatusCode = HttpStatus.CREATED,
                success = true,
                count = 1,
                data = data,
            )
            return ResponseEntity(basicResponse, HttpStatus.CREATED)
        }

        fun <T> created(data: List<T>): ResponseEntity<BasicResponse<List<T>>> {
            val basicResponse = BasicResponse(
                status = HttpStatus.CREATED.value(),
                httpStatusCode = HttpStatus.CREATED,
                success = true,
                count = data.size,
                data = data,
            )
            return ResponseEntity(basicResponse, HttpStatus.CREATED)
        }
    }
}

enum class Result(title: String) {
    FAIL("FAIL"),
    SUCCESS("SUCCESS")
}