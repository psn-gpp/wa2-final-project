package it.wa2.paymentservice.advices

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class PaymentException(message: String) : RuntimeException(message)
class PaymentCreationBadRequest(message: String) : RuntimeException(message)
class PaymentCreationInternalServerError(message: String) : RuntimeException(message)
class PaymentCaptureException(message: String) : RuntimeException(message)
class PaymentNotFoundException(message: String) : RuntimeException(message)

@RestControllerAdvice
class PaymentProblemDetailsHandler: ResponseEntityExceptionHandler() {

    // Bad Request
    @ExceptionHandler(PaymentCreationBadRequest::class)
    fun handlePaymentCreationBadRequest(e: PaymentCreationBadRequest) = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message).apply {
        title = "Payment creation exception"
        detail = e.message
    }

    // Internal Server Error
    @ExceptionHandler(PaymentCreationInternalServerError::class)
    fun handlePaymentCreationInternalServerError(e: PaymentCreationInternalServerError) = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.message).apply {
        title = "Payment creation exception"
        detail = e.message
    }

    @ExceptionHandler(PaymentCaptureException::class)
    fun handlePaymentCaptureException(e: PaymentCaptureException) = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.message).apply {
        title = "Payment creation exception"
        detail = e.message
    }

    @ExceptionHandler(PaymentNotFoundException::class)
    fun handlePaymentNotFoundException(e: PaymentNotFoundException) = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message).apply {
        title = "Payment creation exception"
        detail = e.message
    }
}