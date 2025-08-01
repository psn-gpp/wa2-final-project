package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.entities.Note
import it.wa2.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface NoteRepository: JpaRepository<Note, Long> {
    //fun findAllByVehicle(vehicleId: Vehicle ): List<Note>

    //filter
    fun findAllByVehicle(
        vehicleId: Vehicle,
        pageable: Pageable): Page<Note>

    //filter by date
    fun findAllByVehicleAndDateBetween(
        vehicleId: Vehicle,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable): Page<Note>

    //filter by date and author
    fun findAllByVehicleAndDateBetweenAndAuthorContainsIgnoreCase(
        vehicleId: Vehicle,
        startDate: LocalDate,
        endDate: LocalDate,
        author: String,
        pageable: Pageable): Page<Note>

    //filter by author
    fun findAllByVehicleAndAuthorContainsIgnoreCase(
        vehicleId: Vehicle,
        author: String,
        pageable: Pageable): Page<Note>

    fun findByText(text:String):Note?

    fun existsNoteByTextAndVehicleId(text:String, vehicleId:Long):Boolean
}