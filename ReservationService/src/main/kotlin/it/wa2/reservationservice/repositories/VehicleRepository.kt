package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.entities.CarModel
import it.wa2.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VehicleRepository: JpaRepository<Vehicle, Long> {


    @Query(
        """
        SELECT v 
        FROM Vehicle v
        WHERE (?1 IS NULL OR v.refCarModel = ?1)
        AND (?2 IS NULL OR v.refAvailability.type = ?2) 
        AND (?3 IS NULL OR v.licencePlate = ?3) 
        AND (?4 IS NULL OR v.vin = ?4) 
        AND (?5 IS NULL OR v.kilometers = ?5) 
        AND (?6 IS NULL OR v.pendingCleaning = ?6) 
        AND (?7 IS NULL OR v.pendingMaintenance = ?7)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        refCarModel: CarModel?,
        refAvailability: String?,
        licencePlate: String?,
        vin: String?,
        kilometers: Float?,
        pendingCleaning: Boolean?,
        pendingMaintenance: Boolean?
    ): Page<Vehicle>

    fun existsByLicencePlate(licencePlate: String?): Boolean
    fun findByLicencePlate(licencePlate: String): MutableList<Vehicle>
    fun findAllByRefCarModelId(refCarModelId: Long): List<Vehicle>
}