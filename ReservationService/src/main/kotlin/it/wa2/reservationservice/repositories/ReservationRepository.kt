package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.entities.Reservation
import it.wa2.reservationservice.entities.Status
import it.wa2.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.*

interface ReservationRepository : JpaRepository<Reservation, Long> {

    @Query(
        """
        SELECT r FROM Reservation r
        WHERE (?1 IS NULL OR r.customerId = ?1)
        AND (?2 IS NULL OR r.employeeId = ?2) 
        AND (?3 IS NULL OR r.vehicle.refCarModel.id = ?3) 
        AND (?4 IS NULL OR r.status.status = ?4) 
        AND (:#{#reservationDate == null} = TRUE OR r.reservationDate = ?5)
        AND (:#{#startDate == null} = TRUE OR r.startDate = ?6)
        AND (:#{#endDate == null} = TRUE OR r.endDate = ?7)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        customerId: Long?,
        employeeId: Long?,
        carModelId: Long?,
        status: String?,
        reservationDate: Date?,
        startDate: Date?,
        endDate: Date?
    ): Page<Reservation>

    fun getReservationsByCustomerId(customerId: Long, pageable: Pageable): Page<Reservation>

    @Query(
        """
        SELECT r FROM Reservation r
        WHERE (?1 IS NULL OR r.vehicle.id = ?1)
        AND (?2 IS NULL OR r.status.status = ?2) 
        
     """
    )
    fun getReservationByVehicleAndStatus(vehicleId: Long, status: String): Reservation?

    @Query("""
    SELECT r.vehicle.id, r.startDate, r.endDate
    FROM Reservation r
    WHERE r.vehicle.refCarModel.id = ?1 AND r.startDate > ?2
    """)
    fun findReservationIntervalsByCarModel(carModelId: Long, data:LocalDate=LocalDate.now()): List<Array<Any>>
}