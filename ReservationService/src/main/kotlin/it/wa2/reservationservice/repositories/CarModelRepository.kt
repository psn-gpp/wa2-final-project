package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.entities.CarModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.swing.text.Segment

@Repository
interface CarModelRepository : JpaRepository<CarModel, Long> {
    fun existsByModelAndManufacturer(modelName: String, manufacturerName: String): Boolean

    @Query(
        """
        SELECT c FROM CarModel c
        WHERE (?1 IS NULL OR c.brand = ?1)
        AND (?2 IS NULL OR c.model = ?2) 
        AND (?3 IS NULL OR c.modelYear = ?3) 
        AND (?4 IS NULL OR c.segment = ?4) 
        AND (?5 IS NULL OR c.doorsNo = ?5) 
        AND (?6 IS NULL OR c.seatingCapacity = ?6) 
        AND (?7 IS NULL OR c.luggageCapacity = ?7) 
        AND (?8 IS NULL OR c.category = ?8) 
        AND (?9 IS NULL OR c.manufacturer = ?9)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        brand: String?,
        model: String?,
        modelYear: Int?,
        segment: String?,
        doorsNo: Int?,
        seatingCapacity: Int?,
        luggageCapacity: Float?,
        category: String?,
        manufacturer: String?,
        engine: String?,
        transmission: String?,
        drivetrain: String?,
        safetyFeatures: List<String>?,
        infotainments: List<String>?
    ): Page<CarModel>
}