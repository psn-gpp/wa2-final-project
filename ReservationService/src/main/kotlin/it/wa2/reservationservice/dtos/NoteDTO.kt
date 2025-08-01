package it.wa2.reservationservice.dtos

import it.wa2.reservationservice.controllers.ValidDateFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class NoteDTO (
    val id: Long,

    @field:NotNull
    val vehicleId: Long,

    @field:NotBlank
    val text: String,

    @field:NotBlank
    val author: String,

    @field:ValidDateFormat
    @field:NotBlank
    val date: String  // Date will be formatted as a string
)