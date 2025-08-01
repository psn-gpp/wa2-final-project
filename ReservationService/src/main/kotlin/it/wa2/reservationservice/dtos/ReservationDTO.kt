package it.wa2.reservationservice.dtos

import it.wa2.reservationservice.entities.Reservation
import it.wa2.reservationservice.entities.Status
import it.wa2.reservationservice.entities.Vehicle
import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Past
import org.jetbrains.annotations.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

class ReservationDTO(
    val id: Long,

    @field:NotNull
    var customerId: Long,

    @field:NotNull
    var employeeId: Long,

    @field:Valid
    var vehicleId: Long,

    @field:Valid
    var status: StatusDTO,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @field:NotNull
    var reservationDate: Date,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @field:NotNull
    @field:Future
    var startDate: Date,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @field:NotNull
    @field:Future
    var endDate: Date,

    @field:Valid
    var paymentAmount: Double? = null,

    var version: Long? = null
)/*{
    fun toEntity(): Reservation = Reservation(
        customerId = customerId,
        employeeId = employeeId,
        vehicle = Vehicle(),
        status = status.toEntity(),
        startDate = startDate,
        endDate = endDate,
        reservationDate = reservationDate,
    )
}*/