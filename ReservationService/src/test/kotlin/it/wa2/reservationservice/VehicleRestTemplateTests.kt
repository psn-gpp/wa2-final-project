package it.wa2.reservationservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.MaintenanceDTO
import it.wa2.reservationservice.dtos.NoteDTO
import it.wa2.reservationservice.dtos.VehicleDTO
import it.wa2.reservationservice.entities.CarModel
import it.wa2.reservationservice.entities.Category
import it.wa2.reservationservice.repositories.CarModelRepository
import it.wa2.reservationservice.repositories.CategoryRepository
import it.wa2.reservationservice.repositories.DrivetrainRepository
import it.wa2.reservationservice.repositories.EngineRepository
import it.wa2.reservationservice.repositories.InfotainmentRepository
import it.wa2.reservationservice.repositories.SafetyFeaturesRepository
import it.wa2.reservationservice.repositories.TransmissionRepository
import it.wa2.reservationservice.repositories.VehicleRepository
import it.wa2.reservationservice.services.CarModelServiceImpl
import it.wa2.reservationservice.services.VehicleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

@Disabled("Cannot figure out how to run tests with TestRestTemplate")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = ["classpath:import.sql"])
class VehicleRestTemplateTests : IntegrationTest() {

    @Autowired
    private lateinit var restTemplate : TestRestTemplate

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository
    @Autowired
    private lateinit var carModelRepository: CarModelRepository
    @Autowired
    private lateinit var categoryRepository: CategoryRepository
    @Autowired
    private lateinit var engineRepository: EngineRepository
    @Autowired
    private lateinit var transmissionRepository: TransmissionRepository
    @Autowired
    private lateinit var drivetrainRepository: DrivetrainRepository
    @Autowired
    private lateinit var safetyFeaturesRepository: SafetyFeaturesRepository
    @Autowired
    private lateinit var infotainmentRepository: InfotainmentRepository

    @Autowired
    private lateinit var vehicleService : VehicleService

    @Autowired
    val objectMapper = jacksonObjectMapper()


    @Test
    @Order(0)
    fun setup() {
        println("TEST ---------------- deleting all notes, maintenances and vehicles")
        vehicleService.deleteAll()
        vehicleRepository.deleteAll()
        //carModelRepository.save(carModelDTOtoEntity(carModelDTO))
        println( "TEST ---------------- " )
    }

    companion object {
        var vehicleId: Long = 1L
        var maintenanceId: Long = 1L
        var noteId: Long = 1L
        val SERVER_URL = "http://localhost:8080/api/v1/"
        val carModelDTO = CarModelDTO(
            id = 1L,
            brand = "Toyota",
            model = "Corolla",
            modelYear = 2020,
            segment = "Sedan",
            doorsNo = 5,
            seatingCapacity = 5,
            luggageCapacity = 500F,
            manufacturer = "Toyota",
            category = "category1",
            costPerDay = 10.0,
            motorDisplacement = 1500.0f,
            airConditioning = true,
            engine = "petrol",
            transmission = "manual",
            drivetrain = "FWD",
            safetyFeatures = mutableListOf("Airbags", "Lane Assist"),
            infotainments = mutableListOf("Bluetooth", "USB"),
        )
        val vehicleDTO = VehicleDTO(
            id = 0L,
            licencePlate = "DP188CD",
            vin = "1HGCM82633A123456",
            kilometers = 1000f,
            availability = "available",
            refCarModel = 1L,
            pendingCleaning = false,
            pendingMaintenance = false
        )
        val updated = VehicleDTO(
            id = 1,
            licencePlate = "TEST456",
            vin = "1HGCM82633A123456",
            kilometers = 2000f,
            availability = "rented",
            refCarModel = 1L,
            pendingCleaning = true,
            pendingMaintenance = true
        )
    }

    private fun carModelDTOtoEntity(carModelDTO : CarModelDTO) = CarModel(
        id = carModelDTO.id,
        brand = carModelDTO.brand,
        model = carModelDTO.model,
        modelYear = carModelDTO.modelYear,
        segment = carModelDTO.segment,
        doorsNo = carModelDTO.doorsNo,
        seatingCapacity = carModelDTO.seatingCapacity,
        luggageCapacity = carModelDTO.luggageCapacity,
        manufacturer = carModelDTO.manufacturer,
        costPerDay = carModelDTO.costPerDay,
        motorDisplacement = carModelDTO.motorDisplacement,
        airConditioning = carModelDTO.airConditioning,
        category = categoryRepository.findByCategory(carModelDTO.category)
            ?: throw IllegalArgumentException("Category not found"),
        refEngine = engineRepository.findByType(carModelDTO.engine)
            ?: throw IllegalArgumentException("Engine type not found"),
        refTransmission = transmissionRepository.findByType(carModelDTO.transmission)
            ?: throw IllegalArgumentException("Transmission type not found"),
        refDrivetrain = drivetrainRepository.findByType(carModelDTO.drivetrain)
            ?: throw IllegalArgumentException("Drivetrain type not found"),
        safetyFeatures = carModelDTO.safetyFeatures.mapNotNull {
            safetyFeaturesRepository.findByFeature(it)
        }.toMutableSet(),
        infotainments = carModelDTO.infotainments.mapNotNull {
            infotainmentRepository.findByType(it)
        }.toMutableSet(),
    )

    @Autowired
    lateinit var dataSource: DataSource

    @BeforeEach
    fun logDatasource() {
        println(">>> Connected DB: ${dataSource.connection.metaData.url}")
    }

    @Test
    fun `log database state`() {
        val response = restTemplate.getForEntity("/api/v1/vehicles", String::class.java)
        println("Database state: ${response.body}")
    }


    // THE TESTS CLASS USE A DIFFERENT DATABASE THAN THE MAIN APPLICATION
    // AND I CANNOT MANAGE TO SOLVE THIS PROBLEM - by Giuseppe
/*

    @Test
    @Order(1)
    fun `POST add vehicle successfully`() {

        logger.info("POST add vehicle")

        val response = restTemplate.postForEntity(SERVER_URL + "vehicles", vehicleDTO, VehicleDTO::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        assertThat(response.body?.id).isNotNull
        vehicleId = response.body!!.id!!
    }

    @Test
    @Order(2)
    fun `GET vehicle by id`() {
        logger.info("GET vehicle by id")
        val response = restTemplate.getForEntity(SERVER_URL + "vehicles/2", VehicleDTO::class.java)
        println(response)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.licencePlate).isEqualTo(vehicleDTO.licencePlate)
    }

    @Test
    @Order(3)
    fun `PUT update vehicle`() {
        logger.info("PUT update vehicle")
        restTemplate.put(SERVER_URL + "vehicles/$vehicleId", updated)
        val response = restTemplate.getForEntity(SERVER_URL + "vehicles/$vehicleId", VehicleDTO::class.java)
        assertThat(response.body?.licencePlate).isEqualTo(updated.licencePlate)
    }

    @Test
    @Order(4)
    fun `POST add maintenance`() {
        logger.info("POST add maintenance")
        val maintenance = MaintenanceDTO(
            id = 0L,
            vehicleLicencePlate = updated.licencePlate,
            defect = "Oil change",
            completedMaintenance = false,
            date = LocalDate.now().toString()
        )
        val response = restTemplate.postForEntity(
            SERVER_URL + "vehicles/$vehicleId/maintenances",
            maintenance,
            MaintenanceDTO::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        maintenanceId = response.body!!.id!!
    }

    @Test
    @Order(5)
    fun `GET vehicle maintenance by id`() {
        logger.info("GET vehicle maintenance by id")
        val response = restTemplate.getForEntity(
            SERVER_URL + "vehicles/$vehicleId/maintenances/$maintenanceId",
            MaintenanceDTO::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.defect).isEqualTo("Oil change")
    }

    @Test
    @Order(6)
    fun `POST add note`() {
        logger.info("POST add note")
        val note = NoteDTO(
            id = 0L,
            text = "Vehicle checked.",
            date = LocalDate.now().toString(),
            author = "Inspector",
            vehicleId = vehicleId
        )
        val response = restTemplate.postForEntity(SERVER_URL + "vehicles/$vehicleId/notes", note, NoteDTO::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        noteId = response.body!!.id!!
    }

    @Test
    @Order(7)
    fun `GET notes`() {
        logger.info("GET notes")
        val response = restTemplate.exchange(
            SERVER_URL + "vehicles/$vehicleId/notes",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<Any>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
*/

    @Test
    @Order(8)
    fun `GET vehicles`() {
        logger.info("GET vehicles")
        val response = restTemplate.exchange(
            SERVER_URL + "vehicles",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<Any>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Order(9)
    fun `GET filters`() {
        logger.info("GET filters")
        val response = restTemplate.getForEntity(SERVER_URL + "vehicles/filters", Any::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Order(10)
    fun `DELETE note`() {
        logger.info("DELETE note")
        restTemplate.delete(SERVER_URL + "vehicles/$vehicleId/notes/$noteId")
    }

    @Test
    @Order(11)
    fun `DELETE vehicle`() {
        logger.info("DELETE vehicle")
        restTemplate.delete(SERVER_URL + "vehicles/$vehicleId")
    }
}