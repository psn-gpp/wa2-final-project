package it.wa2.reservationservice.controllers


import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


class CarModelNotFound(message: String) : RuntimeException(message)
class CarModelDuplicate(message: String) : RuntimeException(message)
class CarModelIdInconsistent(message: String) : RuntimeException(message)
class MainteinanceDuplicate(message: String) : RuntimeException(message)

@RestControllerAdvice
class ProblemDetailsHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationErrors(e: ConstraintViolationException) :ProblemDetail{
        val errors : MutableMap<String,String> = HashMap()
        e.constraintViolations.forEach {violation ->
            errors[violation.propertyPath.toString()] = violation.message
        }
        return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
            title = "Validation error"
            this.properties = errors as Map<String,Any>
        }
    }

    @ExceptionHandler(CarModelNotFound::class)
    fun handleCarModelNotFound(e: CarModelNotFound) =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            title = "Model not found"
            detail = e.message
        }

    @ExceptionHandler(CarModelDuplicate::class)
    fun handleCarModelDuplicate(e: CarModelDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
        title = "Duplicate error"
        detail = e.message
    }

    @ExceptionHandler(CarModelIdInconsistent::class)
    fun handleCarModelInconsistent(e: CarModelIdInconsistent) = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        title = "Inconsistent error"
        detail = e.message
    }

    @ExceptionHandler(MainteinanceDuplicate::class)
    fun handleManteinanceDuplicate(e: MainteinanceDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply{
        title = "Duplicate maintenaince error"
        detail = e.message
    }
}



