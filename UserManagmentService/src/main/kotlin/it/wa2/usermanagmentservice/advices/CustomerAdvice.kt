package it.wa2.usermanagmentservice.advices

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class CustomerDuplicate(message: String) : RuntimeException(message)
class CustomerNotFound(message: String) : RuntimeException(message)
class CustomerIdInconsistent(message: String) : RuntimeException(message)

@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(CustomerDuplicate::class)
    fun handleCustomerDuplicate(e: CustomerDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
        title = "Duplicate error"
        detail = e.message
    }

    @ExceptionHandler(CustomerNotFound::class)
    fun handleCustomerNotFound(e: CustomerNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Customer not found"
        detail = e.message
    }

    @ExceptionHandler(CustomerIdInconsistent::class)
    fun handleCustomerIdInconsistent(e: CustomerIdInconsistent) = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        title = "Customer Id inconsistent"
        detail = e.message
    }
}