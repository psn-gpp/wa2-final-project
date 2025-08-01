package it.wa2.usermanagmentservice.repositories

import it.wa2.usermanagmentservice.entities.Employee
import it.wa2.usermanagmentservice.entities.GenericUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GenericUserRepository : JpaRepository<GenericUser, Long> {

    @Query(
        """
        SELECT u FROM GenericUser u
        WHERE (?1 IS NULL OR u.name = ?1)
        AND (?2 IS NULL OR u.surname = ?2) 
        AND (?3 IS NULL OR u.address = ?3) 
        AND (?4 IS NULL OR u.city = ?4)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
    ): Page<GenericUser>

    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean

    fun findByKeycloakId(keycloakId: String): GenericUser?
}

