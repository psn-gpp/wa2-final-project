package it.wa2.reservationservice.controllers

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Payload
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass

@Constraint(validatedBy = [DateFormatValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidDateFormat(
    val message: String = "Invalid date format: Expected format is yyyy-MM-dd",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
class DateFormatValidator : ConstraintValidator<ValidDateFormat, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        //if (value == null) return false

        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
}

class VehicleNotFound(message: String) : RuntimeException(message)
class MaintenanceNotFound(message: String) : RuntimeException(message)
class DuplicatedNote(message: String) : RuntimeException(message)
class VehicleIdInconsistent(message: String) : RuntimeException(message)
class AvailabilityNotFound(message: String) : RuntimeException(message)
class VehicleDuplication(message: String) : RuntimeException(message)
class NoteNotFound(message: String) : RuntimeException(message)

//possibile che debba essere solo uno tra questo e quello del carModel?
@RestControllerAdvice
class ProblemDetailsHandlerVehicles: ResponseEntityExceptionHandler() {

    @ExceptionHandler(MaintenanceNotFound::class)
    fun handleMaintenanceNotFound(e: MaintenanceNotFound) =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            title = "Maintenance not found"
            detail = e.message
        }

    @ExceptionHandler(VehicleNotFound::class)
    fun handleVehicleNotFound(e: VehicleNotFound) =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            title = "Vehicle not found"
            detail = e.message
        }

    @ExceptionHandler(VehicleIdInconsistent::class)
    fun handleVehicleNotFound(e: VehicleIdInconsistent) =
        ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
            title = "VehicleId in URL and in request body are different"
            detail = e.message
        }

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

    @ExceptionHandler(DuplicatedNote::class)
    fun handleDuplicatedNote(e: DuplicatedNote) : ProblemDetail{
        return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
            title = "Duplicated note"
            detail = e.message
        }
    }

    @ExceptionHandler(AvailabilityNotFound::class)
    fun handleAvailabilityNotFound(e : AvailabilityNotFound) : ProblemDetail {
        return ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            title = "Availability not found"
            detail = e.message
        }
    }


    @ExceptionHandler(VehicleDuplication::class)
    fun handleVehicleDuplication(e: VehicleDuplication) : ProblemDetail {
        return ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
            title = "Vehicle duplication"
            detail = e.message
        }
    }

    // handle note not found
    @ExceptionHandler(NoteNotFound::class)
    fun handleNoteNotFound(e: NoteNotFound) =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            title = "Note not found"
            detail = e.message
        }
}