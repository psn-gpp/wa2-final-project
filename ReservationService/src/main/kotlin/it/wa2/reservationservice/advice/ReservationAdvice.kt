package it.wa2.reservationservice.advice

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class ReservationNotFound(message: String) : RuntimeException(message)
class ReservationDuplicate(message: String) : RuntimeException(message)
class StatusNotFound(message: String) : RuntimeException(message)
class UserNotFound(message: String) : RuntimeException(message)
class UnableToProcessEvent(message: String) : RuntimeException(message)
class ReservationNotAccepted(message: String) : RuntimeException(message)


@RestControllerAdvice
class ProblemDetailsReservationHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleEventError(e: UnableToProcessEvent) = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
        title = "Unable to process the event"
        detail = e.message
    }

    @ExceptionHandler
    fun handleReservationNotFound(e: ReservationNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Reservation not found"
        detail = e.message
    }

    @ExceptionHandler(ReservationDuplicate::class)
    fun handleReservationDuplicate(e: ReservationDuplicate) = ProblemDetail.forStatus(HttpStatus.CONFLICT).apply {
        title = "Duplicate error"
        detail = e.message
    }

    @ExceptionHandler
    fun handleStatusNotFound(e: StatusNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "Status not found"
        detail = e.message
    }

    @ExceptionHandler
    fun handleUserNotFound(e: UserNotFound) = ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
        title = "User not found"
        detail = e.message
    }

    @ExceptionHandler
    fun handleReservationNotAccepted(e: ReservationNotAccepted) = ProblemDetail.forStatus(HttpStatus.FORBIDDEN).apply {
        title = "Forbidden operation"
        detail = e.message
    }
}