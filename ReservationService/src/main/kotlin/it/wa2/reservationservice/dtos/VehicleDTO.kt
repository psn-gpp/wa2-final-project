package it.wa2.reservationservice.dtos

import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable

@Serializable
data class VehicleDTO (
    val id: Long?,

    @field:NotNull
    @field:Min(1)
    val refCarModel: Long,

    @field:NotBlank
    val availability: String,
    

    @field:NotBlank
    @field:Size(min = 5, max = 12)
    @field:Pattern(regexp = "^[A-Z0-9 -]*$", message = "License plate can only contain uppercase letters, numbers, spaces and hyphens")
    val licencePlate: String,

    @field:NotBlank
    @field:Size(min = 17, max = 17)
    @field:Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be a valid 17-character Vehicle Identification Number")
    val vin: String,

    @field:NotNull
    @field:Min(0)
    val kilometers: Float,

    @field:NotNull
    val pendingCleaning: Boolean,

    @field:NotNull
    val pendingMaintenance: Boolean,

    /*@field:Size(max = 500)
    val notes: String = ""*/
)/*{
    fun toEntity(): Vehicle = Vehicle(
        id = this.id,
        refCarModel = CarModel(id = this.refCarModel),
        refAvailability = Availability(type = this.availability),
        licencePlate = this.licencePlate,
        vin = this.vin,
        kilometers = this.kilometers,
        pendingCleaning = this.pendingCleaning,
        pendingMaintenance = this.pendingMaintenance
    )
}*/