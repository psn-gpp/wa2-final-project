package it.wa2.usermanagmentservice.advices

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class EmployeeDuplicate(message: String) : RuntimeException(message)
class EmployeeNotFound(message: String) : RuntimeException(message)
class EmployeeInconsistency(message: String) : RuntimeException(message)
class RoleNotFound(message: String) : RuntimeException(message)

@RestControllerAdvice
class ProblemDetailsEmployeeHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(EmployeeDuplicate::class)
    fun handleEmployeeDuplicate(e: EmployeeDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
        title = "Duplicate error"
        detail = e.message
    }

    @ExceptionHandler(EmployeeNotFound::class)
    fun handleEmployeeNotFound(e: EmployeeNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Employee not found"
        detail = e.message
    }

    @ExceptionHandler(EmployeeInconsistency::class)
    fun handleEmployeeInconsistency(e: EmployeeInconsistency) = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        title = "Employee id inconsistent"
        detail = e.message
    }

    @ExceptionHandler(RoleNotFound::class)
    fun handleEmployeeNotFound(e: RoleNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Role not found"
        detail = e.message
    }

}
