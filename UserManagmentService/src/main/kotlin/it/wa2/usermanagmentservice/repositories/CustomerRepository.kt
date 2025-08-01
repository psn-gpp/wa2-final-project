package it.wa2.usermanagmentservice.repositories


import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.entities.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*


interface CustomerRepository : JpaRepository<Customer, Long> {
    @Query(
        """
        SELECT c FROM Customer c
        WHERE (?1 IS NULL OR c.genericUserData.name = ?1)
        AND (?2 IS NULL OR c.genericUserData.surname = ?2) 
        AND (?3 IS NULL OR c.genericUserData.address = ?3) 
        AND (?4 IS NULL OR c.genericUserData.city = ?4) 
        AND (:#{#dateOfBirth == null} = TRUE OR c.dateOfBirth = ?5) 
        AND (?6 IS NULL OR c.reliabilityScores = ?6)
        AND (?7 IS NULL OR c.drivingLicense = ?7) 
        AND (:#{#expirationDate == null} = TRUE OR c.expirationDate = :expirationDate)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        dateOfBirth: Date?,
        reliabilityScores: Int?,
        drivingLicence: String?,
        expirationDate: Date?,
    ): Page<Customer>

    fun existsByDrivingLicense(drivingLicence: String): Boolean


}

