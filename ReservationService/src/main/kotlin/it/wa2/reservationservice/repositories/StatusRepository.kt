package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.entities.Status
import org.springframework.data.jpa.repository.JpaRepository

interface StatusRepository : JpaRepository<Status, Long> {
    fun findFirstByStatus(status: String): Status?
}