package it.wa2.reservationservice.entities

import it.wa2.reservationservice.dtos.ReservationDTO
import jakarta.persistence.*
import java.util.*

@Entity
class Reservation(
    var customerId: Long,
    var employeeId: Long,

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    var vehicle: Vehicle,

    @ManyToOne
    @JoinColumn(name = "status_id")
    var status: Status,

    @Temporal(TemporalType.TIMESTAMP)
    var reservationDate: Date,

    @Temporal(TemporalType.TIMESTAMP)
    var startDate: Date,

    @Temporal(TemporalType.TIMESTAMP)
    var endDate: Date,

    var paymentAmount: Double,

) : BaseEntity(){
    @Version
    var version: Long? = null

    fun toDTO(): ReservationDTO = ReservationDTO(
        id = this.id,
        customerId = customerId,
        employeeId = employeeId,
        vehicleId = vehicle.id,
        status = status.toDTO(),
        startDate = startDate,
        endDate = endDate,
        reservationDate = reservationDate,
        version = version,
        paymentAmount = paymentAmount
    )

    fun update(newReservation: Reservation){
        this.id = newReservation.id
        this.customerId = newReservation.customerId
        this.employeeId = newReservation.employeeId
        this.vehicle = newReservation.vehicle
        this.status = newReservation.status
        this.startDate = newReservation.startDate
        this.endDate = newReservation.endDate
        this.reservationDate = newReservation.reservationDate
        this.paymentAmount = newReservation.paymentAmount
    }
}