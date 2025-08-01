package it.wa2.reservationservice.repositories

//import it.wa2.reservationservice.dtos.AvailabilityDTO
import it.wa2.reservationservice.entities.Availability
import org.springframework.data.jpa.repository.JpaRepository

interface AvailabilityRepository : JpaRepository<Availability, Long> {
    fun  existsByType(type:String):Boolean
    fun findByType(type:String):Availability?
}