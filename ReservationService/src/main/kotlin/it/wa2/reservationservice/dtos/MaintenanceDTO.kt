package it.wa2.reservationservice.dtos

import it.wa2.reservationservice.controllers.ValidDateFormat
import it.wa2.reservationservice.entities.MaintenanceHistory
import it.wa2.reservationservice.entities.Vehicle
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size


data class MaintenanceDTO (
    val id: Long = 0L,
    val vehicleId: Long = 0L,

    @field:NotBlank
    @field:Size(min = 5, max = 12)
    val vehicleLicencePlate: String,

    @field:NotBlank
    val defect: String,

    @field:NotNull
    val completedMaintenance: Boolean = false,

    @field:ValidDateFormat
    val date: String?
)

