package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.entities.MaintenanceHistory
import it.wa2.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MaintenanceRepository: JpaRepository<MaintenanceHistory, Long> {
    fun getAllByVehicle(vehicle: Vehicle): List<MaintenanceHistory>
    fun existsByVehicleAndId(vehicle: Vehicle, id: Long):Boolean

    @Query(
        """
            SELECT m
            FROM MaintenanceHistory m
            WHERE ( m.vehicle.id = ?1)
                AND (?2 IS NULL OR m.vehicle.licencePlate = ?2) 
                AND (?3 IS NULL OR m.defect = ?3) 
                AND (?4 IS NULL OR m.completedMaintenance = ?4) 
                AND (?5 IS NULL OR m.date = ?5) 
        """
    )
    fun findWithFilters(
        pageable: Pageable,
        vehicleId: Long,
        vehicleLicencePlate: String?,
        defect: String?,
        completedMaintenance: Boolean?,
        date: String?
    ): Page<MaintenanceHistory>
}