package it.wa2.reservationservice.dtos

import it.wa2.reservationservice.entities.CarModel
import it.wa2.reservationservice.repositories.CategoryRepository
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


data class CarModelDTO(
    val id: Long,

    @field:NotBlank
    val brand: String,

    @field:NotBlank
    val model: String,

    @field:NotNull
    val modelYear: Int,

    @field:NotBlank
    val segment: String,

    @field:NotNull
    val doorsNo: Int,

    @field:NotNull
    val seatingCapacity: Int,

    @field:NotNull
    val luggageCapacity: Float,

    @field:NotBlank
    @field:NotEmpty
    val manufacturer: String,

    @field:NotNull
    val costPerDay: Double,

    @field:NotNull
    val motorDisplacement: Float,

    @field:NotNull
    val airConditioning: Boolean,

    @field:NotBlank
    val category: String,

    @field:NotBlank
    val engine: String,

    @field:NotBlank
    val transmission: String,

    @field:NotBlank
    val drivetrain: String,

    @field:NotEmpty
    val safetyFeatures: MutableList<String> = mutableListOf(),

    @field:NotEmpty
    val infotainments: MutableList<String> = mutableListOf(),
)

