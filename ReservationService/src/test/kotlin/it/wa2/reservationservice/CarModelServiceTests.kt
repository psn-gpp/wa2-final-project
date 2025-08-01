package it.wa2.reservationservice

import it.wa2.reservationservice.controllers.CarModelDuplicate
import it.wa2.reservationservice.controllers.CarModelNotFound
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.repositories.CategoryRepository
import it.wa2.reservationservice.repositories.DrivetrainRepository
import it.wa2.reservationservice.repositories.EngineRepository
import it.wa2.reservationservice.repositories.InfotainmentRepository
import it.wa2.reservationservice.repositories.SafetyFeaturesRepository
import it.wa2.reservationservice.repositories.TransmissionRepository
import it.wa2.reservationservice.services.CarModelService
import it.wa2.reservationservice.services.CarModelServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer

private val logger = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = ["classpath:import.sql"])
class CarModelServiceImplTest : IntegrationTest() {
    @Autowired
    private lateinit var carModelService: CarModelService

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

    private lateinit var testCarModel: CarModelDTO

    @BeforeEach
    fun setup() {
        testCarModel = CarModelDTO(
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
    }

    @Test
    fun `test getFilters returns all available options`() {
        val filters = carModelService.getFilters()

        assertAll(
            { assertTrue(filters.categories.contains("category1")) },
            { assertTrue(filters.engines.contains("petrol")) },
            { assertTrue(filters.transmissions.contains("automatic")) },
            { assertTrue(filters.drivetrains.contains("FWD")) },
            { assertTrue(filters.safetyFeatures.containsAll(listOf("feature1", "feature2"))) },
            { assertTrue(filters.infotainments.containsAll(listOf("USB", "radio"))) }
        )
    }

    @Test
    fun `test addModel successfully adds new car model`() {
        val savedModel = carModelService.addModel(testCarModel)

        assertAll(
            { assertTrue(savedModel.id > 0) },
            { assertEquals(testCarModel.brand, savedModel.brand) },
            { assertEquals(testCarModel.model, savedModel.model) }
        )
    }

    @Test
    fun `test addModel throws CarModelDuplicate when model already exists`() {
        carModelService.addModel(testCarModel)

        assertThrows<CarModelDuplicate> {
            carModelService.addModel(testCarModel)
        }
    }

    @Test
    fun `test getModelById returns correct model`() {
        val savedModel = carModelService.addModel(testCarModel)
        val retrievedModel = carModelService.getModelById(savedModel.id)

        assertEquals(savedModel, retrievedModel)
    }

    @Test
    fun `test getModelById throws CarModelNotFound for non-existent id`() {
        assertThrows<CarModelNotFound> {
            carModelService.getModelById(999L)
        }
    }

    @Test
    fun `test modifyCarModel updates existing model`() {
        val savedModel = carModelService.addModel(testCarModel)
        val modifiedModel = savedModel.copy(
            brand = "Honda",
            model = "Civic"
        )

        carModelService.modifyCarModel(savedModel.id, modifiedModel)

        val updatedModel = carModelService.getModelById(savedModel.id)
        assertEquals("Honda", updatedModel.brand)
        assertEquals("Civic", updatedModel.model)
    }

    @Test
    fun `test modifyCarModel throws CarModelNotFound for non-existent id`() {
        assertThrows<CarModelNotFound> {
            carModelService.modifyCarModel(999L, testCarModel.copy(id = 999L))
        }
    }

    @Test
    fun `test deleteModelById successfully deletes model`() {
        val savedModel = carModelService.addModel(testCarModel)
        carModelService.deleteModelById(savedModel.id)

        assertThrows<CarModelNotFound> {
            carModelService.getModelById(savedModel.id)
        }
    }

    @Test
    fun `test getModels with filters returns correct results`() {
        val savedModel = carModelService.addModel(testCarModel)
        val pageable = PageRequest.of(0, 10)

        val resultAll = carModelService.getModels(
            pageable = pageable,
            brand = null, model = null, modelYear = null,
            segment = null, doorsNo = null, seatingCapacity = null,
            luggageCapacity = null, category = null, manufacturer = null,
            engine = null, transmission = null, drivetrain = null,
            safetyFeatures = null, infotainments = null
        )
        assertEquals(1, resultAll.content.size)

        val resultFiltered = carModelService.getModels(
            pageable = pageable,
            brand = "Toyota", model = null, modelYear = null,
            segment = null, doorsNo = null, seatingCapacity = null,
            luggageCapacity = null, category = null, manufacturer = null,
            engine = null, transmission = null, drivetrain = null,
            safetyFeatures = null, infotainments = null
        )
        assertEquals(1, resultFiltered.content.size)

        val resultEmpty = carModelService.getModels(
            pageable = pageable,
            brand = "NonExistent", model = null, modelYear = null,
            segment = null, doorsNo = null, seatingCapacity = null,
            luggageCapacity = null, category = null, manufacturer = null,
            engine = null, transmission = null, drivetrain = null,
            safetyFeatures = null, infotainments = null
        )
        assertEquals(0, resultEmpty.content.size)
    }

    @Test
    fun `test addModel with invalid category throws IllegalArgumentException`() {
        val invalidModel = testCarModel.copy(category = "nonexistent_category")

        assertThrows<IllegalArgumentException> {
            carModelService.addModel(invalidModel)
        }
    }

    @Test
    fun `test addModel with invalid engine throws IllegalArgumentException`() {
        val invalidModel = testCarModel.copy(engine = "nonexistent_engine")

        assertThrows<IllegalArgumentException> {
            carModelService.addModel(invalidModel)
        }
    }
}