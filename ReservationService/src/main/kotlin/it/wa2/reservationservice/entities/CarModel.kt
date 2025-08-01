package it.wa2.reservationservice.entities

import it.wa2.reservationservice.dtos.CarModelDTO
import jakarta.persistence.*

@Entity
class CarModel (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,
    val brand: String = "",
    val model: String = "",
    val modelYear: Int = 0,
    val segment: String = "",
    val doorsNo: Int = 0,
    val seatingCapacity: Int = 0,
    val luggageCapacity: Float = 0.0F,
    val manufacturer: String = "",
    val costPerDay: Double = 0.0,
    val motorDisplacement: Float = 0.0F,
    val airConditioning: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "ref_category"/*, referencedColumnName = "id"*/)
    val category: Category,

    @ManyToOne
    @JoinColumn(name = "ref_engine"/*, referencedColumnName = "id"*/)
    val refEngine: Engine,

    @ManyToOne
    @JoinColumn(name = "ref_transmission"/*, referencedColumnName = "id"*/)
    val refTransmission: Transmission,

    @ManyToOne
    @JoinColumn(name = "ref_drivetrain"/*, referencedColumnName = "id"*/)
    val refDrivetrain: Drivetrain,

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinTable(
        name = "car_model_safety_features",
        joinColumns = [JoinColumn(name = "car_model_id")],
        inverseJoinColumns = [JoinColumn(name = "safety_feature_id")]
    )
    val safetyFeatures: MutableSet<SafetyFeatures> = mutableSetOf(),

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinTable(
        name = "car_model_infotainments",
        joinColumns = [JoinColumn(name = "car_model_id")],
        inverseJoinColumns = [JoinColumn(name = "infotainment_id")]
    )
    val infotainments: MutableSet<Infotainment> = mutableSetOf(),


)

fun CarModel.toDTO() = CarModelDTO(
    id = this.id,
    brand = this.brand,
    model = this.model,
    modelYear = this.modelYear,
    segment = this.segment,
    doorsNo = this.doorsNo,
    seatingCapacity = this.seatingCapacity,
    luggageCapacity = this.luggageCapacity,
    category = this.category.category,
    manufacturer = this.manufacturer,
    costPerDay = this.costPerDay,
    motorDisplacement = this.motorDisplacement,
    airConditioning = this.airConditioning,
    engine = this.refEngine.type,
    transmission = this.refTransmission.type,
    drivetrain = this.refDrivetrain.type,
    safetyFeatures = this.safetyFeatures.map { it.feature }.toMutableList(),
    infotainments = this.infotainments.map { it.type }.toMutableList()
)
