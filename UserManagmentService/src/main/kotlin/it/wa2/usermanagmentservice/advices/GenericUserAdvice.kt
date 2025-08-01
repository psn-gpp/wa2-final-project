package it.wa2.usermanagmentservice.advices

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class GenericUserDuplicate(message: String) : RuntimeException(message)
class GenericUserNotFound(message: String) : RuntimeException(message)
class GenericUserInconsistency(message: String) : RuntimeException(message)
@RestControllerAdvice
class ProblemDetailsUserHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(GenericUserDuplicate::class)
    fun handleGenericUserDuplicate(e: GenericUserDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
        title = "Duplicate error"
        detail = e.message
    }

    @ExceptionHandler(GenericUserNotFound::class)
    fun handleUserNotFound(e: GenericUserNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Generic user not found"
        detail = e.message
    }

    @ExceptionHandler(GenericUserInconsistency::class)
    fun handleEmployeeInconsistency(e: GenericUserInconsistency) = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        title = "Generic user id inconsistent"
        detail = e.message
    }
}