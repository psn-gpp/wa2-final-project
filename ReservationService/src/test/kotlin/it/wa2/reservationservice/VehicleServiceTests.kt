package it.wa2.reservationservice

import it.wa2.reservationservice.controllers.*
import it.wa2.reservationservice.dtos.*
import it.wa2.reservationservice.repositories.AvailabilityRepository
import it.wa2.reservationservice.repositories.CarModelRepository
import it.wa2.reservationservice.services.CarModelServiceImpl
import it.wa2.reservationservice.services.VehicleService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = LoggerFactory.getLogger(VehicleServiceImplTest::class.java)

@SpringBootTest(properties = ["spring.flyway.enabled=false"])
@Transactional
@Sql(scripts = ["classpath:import.sql"])
class VehicleServiceImplTest : IntegrationTest() {


    @Autowired
    private lateinit var availabilityRepository: AvailabilityRepository

    @Autowired
    private lateinit var carModelServiceImpl: CarModelServiceImpl

    @Autowired
    private lateinit var vehicleService: VehicleService

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    private lateinit var testVehicle: VehicleDTO
    private lateinit var savedCarModel: CarModelDTO
    private lateinit var testNote: NoteDTO
    private lateinit var testMaintenance: MaintenanceDTO

    @BeforeEach
    fun setup() {
        // Setup a CarModelDTO for testing
        val newCarMode = CarModelDTO(
            id = 0L,
            brand = "Toyota",
            model = "Corolla",
            modelYear = 2020,
            segment = "Sedan",
            doorsNo = 5,
            seatingCapacity = 5,
            luggageCapacity = 500F,
            manufacturer = "Toyota",
            costPerDay = 10.0,
            motorDisplacement = 1800F,
            airConditioning = true,
            category = "category1",
            engine = "petrol",
            transmission = "automatic",
            drivetrain = "FWD",
            safetyFeatures = mutableListOf("feature1", "feature2"),
            infotainments = mutableListOf("USB", "radio")
        )

        savedCarModel = carModelServiceImpl.addModel(newCarMode)

        testVehicle = VehicleDTO(
            id = 0L,
            refCarModel = savedCarModel.id,
            licencePlate = "XY123Z",
            vin = "1HGCM82633A123456",
            availability = "available",
            kilometers = 15000f,
            pendingCleaning = false,
            pendingMaintenance = false
        )

        testNote = NoteDTO(
            id = 0L,
            vehicleId = 1L,
            author = "Test Author",
            text = "Test Note",
            date = LocalDate.now().toString()
        )

        testMaintenance = MaintenanceDTO(
            id = 0L,
            defect = "Test Defect",
            date = LocalDate.now().toString(),
            completedMaintenance = false,
            vehicleId = 0L,
            vehicleLicencePlate = "XY123Z"
        )
    }

    @Test
    fun `add vehicle successfully`() {

        val savedVehicle = vehicleService.addVehicle(testVehicle)
        assertAll(
            { assertTrue(savedVehicle.id!! > 0) },
            { assertEquals(testVehicle.licencePlate, savedVehicle.licencePlate) },
            { assertEquals(testVehicle.vin, savedVehicle.vin) },
            { assertEquals(testVehicle.availability, savedVehicle.availability) }
        )
    }

    @Test
    fun `fetch vehicle by ID`() {

        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val retrievedVehicle = vehicleService.getVehicleById(savedVehicle.id!!)
        assertEquals(savedVehicle, retrievedVehicle)
    }

    @Test
    fun `fetch vehicles with pagination and filters`() {

        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val pageable = PageRequest.of(0, 5)

        val allVehicles = vehicleService.getVehicles(
            pageable = pageable,
            refCarModel = null,
            availability = null,
            licencePlate = null,
            vin = null,
            kilometers = null,
            pendingCleaning = null,
            pendingMaintenance = null
        )

        val filteredVehicles = vehicleService.getVehicles(
            pageable = pageable,
            refCarModel = savedVehicle.refCarModel,
            availability = savedVehicle.availability,
            licencePlate = savedVehicle.licencePlate,
            vin = savedVehicle.vin,
            kilometers = null,
            pendingCleaning = null,
            pendingMaintenance = null
        )

        assertAll(
            { assertEquals(1, allVehicles.content.size) },
            { assertEquals(1, filteredVehicles.content.size) },
            { assertEquals(savedVehicle, filteredVehicles.content[0]) }
        )
    }

    @Test
    fun `add and fetch vehicle note`() {


        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val vehicles = vehicleService.getVehicles(
            pageable = PageRequest.of(0, 5),
            refCarModel = null,
            availability = null,
            licencePlate = null,
            vin = null,
            kilometers = null,
            pendingCleaning = null,
            pendingMaintenance = null
        )

        assert(vehicles.content.isNotEmpty()) {"No vehicles found" }

        val savedNote = vehicleService.addVehicleNote(savedVehicle.id!!, testNote.copy(vehicleId = savedVehicle.id!!.toLong()))

        val fetchedNotes = vehicleService.getVehicleNotes(
            vehicleId = savedVehicle.id!!,
            pageable = PageRequest.of(0, 5),
            startDate = null,
            endDate = null,
            author = null
        )

        assertAll(
            { assertEquals(1, fetchedNotes.content.size) },
            { assertEquals(savedNote, fetchedNotes.content[0]) }
        )
    }

    @Test
    fun `update vehicle successfully`() {


        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val updatedVehicle = testVehicle.copy(
            id = savedVehicle.id!!,
            kilometers = 20000f,
            pendingMaintenance = true
        )

        vehicleService.modifyVehicle(savedVehicle.id!!, updatedVehicle)

        val retrievedVehicle = vehicleService.getVehicleById(savedVehicle.id!!)
        assertAll(
            { assertEquals(20000f, retrievedVehicle.kilometers) },
            { assertTrue(retrievedVehicle.pendingMaintenance) }
        )
    }

    @Test
    fun `delete vehicle successfully`() {


        val savedVehicle = vehicleService.addVehicle(testVehicle)
        vehicleService.deleteVehicleById(savedVehicle.id!!)

        assertThrows<VehicleNotFound> {
            vehicleService.getVehicleById(savedVehicle.id!!)
        }
    }

    @Test
    fun `create, update, and fetch vehicle maintenance`() {


        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val savedMaintenance = vehicleService.addVehicleMaintenance(savedVehicle.id!!, testMaintenance)

        val updatedMaintenance = savedMaintenance.copy(
            completedMaintenance = true,
            defect = "Updated defect"
        )

        vehicleService.modifyVehicleMaintenance(savedVehicle.id!!, updatedMaintenance)

        val retrievedMaintenance = vehicleService.getVehicleMaintenanceById(savedVehicle.id!!, savedMaintenance.id)
        assertAll(
            { assertEquals(true, retrievedMaintenance.completedMaintenance) },
            { assertEquals("Updated defect", retrievedMaintenance.defect) }
        )
    }

    @Test
    fun `fetch vehicle maintenances using filters`() {


        val savedVehicle = vehicleService.addVehicle(testVehicle)
        val savedMaintenance = vehicleService.addVehicleMaintenance(savedVehicle.id!!, testMaintenance)

        val maintenances = vehicleService.getVehicleMaintenances(
            pageable = PageRequest.of(0, 5),
            vehicleId = savedVehicle.id!!,
            vehicleLicencePlate = savedVehicle.licencePlate,
            defect = null,
            completedMaintenance = false,
            date = null
        )

        assertAll(
            { assertEquals(1, maintenances.content.size) },
            { assertEquals(savedMaintenance, maintenances.content[0]) }
        )
    }

    @Test
    fun `fetch available filters`() {


        val availabilities = availabilityRepository.findAll()
        assertTrue(availabilities.isNotEmpty())

        val filters = vehicleService.getFilters()
        assertNotNull(filters)
        assertTrue(filters.availabilities.isNotEmpty())
    }
}