package it.wa2.reservationservice.entities

import it.wa2.reservationservice.controllers.ValidDateFormat
import it.wa2.reservationservice.dtos.MaintenanceDTO
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
class MaintenanceHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(
        name = "ref_vehicle",
        foreignKey = ForeignKey(
            name = "fk_vehicle_maintenance",
            foreignKeyDefinition = "FOREIGN KEY (ref_vehicle) REFERENCES vehicle(id) ON DELETE CASCADE"
        )
    )
    val vehicle: Vehicle,

    val defect: String = "",
    val completedMaintenance: Boolean = false,
    val date: LocalDate? = null,
)

fun MaintenanceHistory.toDTO() = MaintenanceDTO(
    id = this.id,
    vehicleId = this.vehicle.id,
    vehicleLicencePlate = this.vehicle.licencePlate,
    defect = this.defect,
    completedMaintenance = this.completedMaintenance,
    date = this.date.toString()
)
