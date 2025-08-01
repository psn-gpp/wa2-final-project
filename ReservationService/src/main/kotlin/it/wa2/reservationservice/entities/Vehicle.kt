package it.wa2.reservationservice.entities

import it.wa2.reservationservice.dtos.VehicleDTO
import jakarta.persistence.*

@Entity
class Vehicle(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(
        name = "ref_car_model",
        foreignKey = ForeignKey(
            name = "fk_vehicle_car_model",
            foreignKeyDefinition = "FOREIGN KEY (ref_car_model) REFERENCES car_model(id) ON DELETE CASCADE"
        )
    )
    var refCarModel: CarModel,

    @ManyToOne
    @JoinColumn(name = "ref_availability")
    var refAvailability: Availability,

    var licencePlate: String = "",
    var vin: String = "",
    var kilometers: Float = 0F,
    var pendingCleaning: Boolean = false,
    var pendingMaintenance: Boolean = false,
    //val notes: String = ""
)

fun Vehicle.toDTO()= VehicleDTO(
    this.id,
    this.refCarModel.id,
    this.refAvailability.type,
    this.licencePlate,
    this.vin,
    this.kilometers,
    this.pendingCleaning,
    this.pendingMaintenance
)
