package it.wa2.reservationservice.services

import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.entities.Status
import it.wa2.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI
import java.time.LocalDate
import java.util.*

interface ReservationService {
    fun getReservations(
        pageable: Pageable,
        customerId: Long?,
        employeeId: Long?,
        carModelId: Long?,
        status: String?,
        reservationDate: Date?,
        startDate: Date?,
        endDate: Date?,
    ): Page<ReservationDTO>

    fun getReservationById(
        reservationId: Long
    ): ReservationDTO

    fun addReservation(
        reservation: ReservationDTO
    ): ReservationDTO

    fun updateReservationById(
        reservationId: Long,
        reservation: ReservationDTO
    ): ReservationDTO

    fun deleteReservationById(
        reservationId: Long
    )

    fun getReservationsByUserId(
        pageable: Pageable,
        userId: Long,
    ): Page<ReservationDTO>

    fun payReservation(reservationId : Long) : String
    fun getFullyBookedDates(carModelId: Long): Set<LocalDate>

}