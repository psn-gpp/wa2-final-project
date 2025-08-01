package it.wa2.reservationservice.entities

import it.wa2.reservationservice.dtos.NoteDTO
import jakarta.persistence.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Entity
class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "ref_vehicle")
    val vehicle: Vehicle,

    val text:String="",
    val author:String,    //////////controllare
    val date: LocalDate
)

fun Note.toDto()= NoteDTO(
    this.id,
    this.vehicle.id,
    this.text,
    this.author,
    this.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
)