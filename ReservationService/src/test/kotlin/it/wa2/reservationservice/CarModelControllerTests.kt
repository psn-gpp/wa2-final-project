package it.wa2.reservationservice

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import it.wa2.reservationservice.controllers.CarModelNotFound
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.CarModelFiltersDTO
import it.wa2.reservationservice.services.CarModelService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

// ########################### CAR MODELS #######################################

@SpringBootTest
@AutoConfigureMockMvc
class TestCarModelController : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc : MockMvc
    
    @MockkBean
    private lateinit var carModelService : CarModelService

    val SERVER_API = "/api/v1/models"


    @Test
    fun `GET models empty`() {
        every {
            carModelService.getModels(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns PageImpl(emptyList())

        mockMvc.get("${SERVER_API}?page=&size=") {
            accept = MediaType.APPLICATION_JSON
            param("brand", "Toyota")
            param("model", "Corolla")
            param("modelYear", "2020")
            param("segment", "Sedan")
            param("doorsNo", "5")
            param("seatingCapacity", "5")
            param("luggageCapacity", "500F")
            param("manufacturer", "Toyota")
            param("costPerDay", "10.0")
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
            }
            content {
                jsonPath("content") {
                    isArray()
                    isEmpty()
                }
            }
        }

        verify {
            carModelService.getModels(
                any(),
                eq("Toyota"),
                eq("Corolla"),
                eq(2020),
                eq("Sedan"),
                eq(5),
                eq(5),
                eq(500f),
                any(),
                eq("Toyota"),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `GET models returns paginated results`() {
        val testModel = CarModelDTO(
            1L,
            "Toyota",
            "Camry",
            2022,
            "Sedan",
            4,
            5,
            450f,
            "Toyota",
            70.0,
            2500f,
            true,
            "category1",
            "petrol",
            "automatic",
            "FWD",
            listOf("feature1") as MutableList<String>,
            listOf("radio") as MutableList<String>
        )
        val page = PageImpl(listOf(testModel))
        every {
            carModelService.getModels(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns page

        mockMvc.get("${SERVER_API}?page=0&size=10") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.content") { isArray() }
                jsonPath("$.content.length()") { value(1) }
            }
        }

        verify {
            carModelService.getModels(
                any(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }
    }

    @Test
    fun `GET model by id returns model when it exists`() {
        val carModelId = 1L
        val testModel = CarModelDTO(
            carModelId,
            "Toyota",
            "Camry",
            2022,
            "Sedan",
            4,
            5,
            450f,
            "Toyota",
            70.0,
            2500f,
            true,
            "category1",
            "petrol",
            "automatic",
            "FWD",
            listOf("feature1") as MutableList<String>,
            listOf("radio") as MutableList<String>
        )
        every { carModelService.getModelById(carModelId) } returns testModel

        mockMvc.get("${SERVER_API}/$carModelId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { value(carModelId) }
            }
        }

        verify { carModelService.getModelById(carModelId) }
    }

    @Test
    fun `GET model by id returns 404 when model does not exist`() {
        val nonExistentId = 999L
        every { carModelService.getModelById(nonExistentId) } throws CarModelNotFound("Model not found")

        mockMvc.get("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }

        verify { carModelService.getModelById(nonExistentId) }
    }

    @Test
    fun `POST add model creates a new car model`() {
        val newModelJson = """
        {
            "id": 0,
            "brand": "Honda",
            "model": "Civic",
            "modelYear": 2021,
            "segment": "Compact",
            "doorsNo": 4,
            "seatingCapacity": 5,
            "luggageCapacity": 400.0,
            "manufacturer": "Honda",
            "costPerDay": 60.0,
            "motorDisplacement": 2000.0,
            "airConditioning": true,
            "category": "category1",
            "engine": "diesel",
            "transmission": "manual",
            "drivetrain": "AWD",
            "safetyFeatures": ["feature1", "feature3"],
            "infotainments": ["USB", "radio"]
        }
    """.trimIndent()

        val returnedModel = CarModelDTO(
            1L,
            "Honda",
            "Civic",
            2021,
            "Compact",
            4,
            5,
            400f,
            "Honda",
            60.0,
            2000f,
            true,
            "category1",
            "diesel",
            "manual",
            "AWD",
            listOf("feature1", "feature3") as MutableList<String>,
            listOf("USB", "radio") as MutableList<String>
        )
        every { carModelService.addModel(any()) } returns returnedModel

        mockMvc.post(SERVER_API) {
            contentType = MediaType.APPLICATION_JSON
            content = newModelJson
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { value(1) }
            }
        }

        verify { carModelService.addModel(any()) }
    }

    @Test
    fun `PUT update model updates the car model when it exists`() {
        val carModelId = 1L
        val updatedModelJson = """
        {
            "id": $carModelId,
            "brand": "Toyota",
            "model": "Camry",
            "modelYear": 2022,
            "segment": "Sedan", 
            "doorsNo": 4,
            "seatingCapacity": 5,
            "luggageCapacity": 450.0,
            "manufacturer": "Toyota",
            "costPerDay": 70.0,
            "motorDisplacement": 2500.0,
            "airConditioning": true,
            "category": "category2",
            "engine": "petrol",
            "transmission": "automatic",
            "drivetrain": "FWD",
            "safetyFeatures": ["feature2", "feature1"],
            "infotainments": ["USB", "radio"]
        }
    """.trimIndent()

        every { carModelService.modifyCarModel(eq(carModelId), any()) } just runs

        mockMvc.put("${SERVER_API}/$carModelId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedModelJson
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify { carModelService.modifyCarModel(eq(carModelId), any()) }
    }

    @Test
    fun `PUT update model returns 404 when car model does not exist`() {
        val nonExistentId = 999L
        val updatedModelJson = """
        {
            "id": $nonExistentId,
            "brand": "Toyota",
            "model": "Camry",
            "modelYear": 2022,
            "segment": "Sedan",
            "doorsNo": 4, 
            "seatingCapacity": 5,
            "luggageCapacity": 450.0,
            "manufacturer": "Toyota",
            "costPerDay": 70.0,
            "motorDisplacement": 2500.0,
            "airConditioning": true,
            "category": "Luxury",
            "engine": "Hybrid",
            "transmission": "Automatic",
            "drivetrain": "FWD",
            "safetyFeatures": ["Airbags", "Lane Assist"],
            "infotainments": ["Bluetooth", "Touchscreen"]
        }
    """.trimIndent()

        every { carModelService.modifyCarModel(eq(nonExistentId), any()) } throws CarModelNotFound("Model not found")

        mockMvc.put("${SERVER_API}/$nonExistentId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedModelJson
        }.andExpect {
            status { isNotFound() }
        }

        verify { carModelService.modifyCarModel(eq(nonExistentId), any()) }
    }

    @Test
    fun `DELETE model by id returns 404 when model does not exist`() {
        val nonExistentId = 999L
        every { carModelService.deleteModelById(nonExistentId) } throws CarModelNotFound("Model not found")

        mockMvc.delete("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }

        verify { carModelService.deleteModelById(nonExistentId) }
    }

    @Test
    fun `DELETE model by id deletes the model when it exists`() {
        val carModelId = 1L
        every { carModelService.deleteModelById(carModelId) } just runs

        mockMvc.delete("${SERVER_API}/$carModelId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify { carModelService.deleteModelById(carModelId) }
    }

    @Test
    fun `GET filters returns all available filters`() {
        val filters = CarModelFiltersDTO(
            categories = listOf("category1", "category2") as MutableList<String>,
            engines = listOf("petrol", "diesel") as MutableList<String>,
            transmissions = listOf("manual", "automatic") as MutableList<String>,
            drivetrains = listOf("FWD", "AWD") as MutableList<String>,
            safetyFeatures = listOf("feature1", "feature2") as MutableList<String>,
            infotainments = listOf("USB", "radio") as MutableList<String>
        )
        every { carModelService.getFilters() } returns filters

        mockMvc.get("${SERVER_API}/filters") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.categories") { isArray() }
                jsonPath("$.engines") { isArray() }
                jsonPath("$.transmissions") { isArray() }
                jsonPath("$.drivetrains") { isArray() }
                jsonPath("$.safetyFeatures") { isArray() }
                jsonPath("$.infotainments") { isArray() }
            }
        }

        verify { carModelService.getFilters() }
    }
}



/*

@Test
    fun `GET models empty`() {
        mockMvc.get("${ SERVER_API }?page=&size=") {
            accept = MediaType.APPLICATION_JSON
            param("brand", "Toyota")
            param("model", "Corolla")
            param("modelYear", "2020")
            param("segment", "Sedan")
            param("doorsNo", "5")
            param("seatingCapacity", "5")
            param("luggageCapacity", "500F")
            param("manufacturer", "Toyota")
            param("costPerDay", "10.0")
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
            }
            content {
                jsonPath("content") {
                    isArray()
                    isEmpty()
                }
            }
        }
    }

    @Test
    fun `GET models returns paginated results`() {
        mockMvc.get("${SERVER_API}?page=0&size=10") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.content") { isArray() }
                jsonPath("$.content.length()") { isNumber() }
            }
        }
    }

    @Test
    fun `GET model by id returns model when it exists`() {
        val carModelId = 1L
        mockMvc.get("${SERVER_API}/$carModelId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { value(carModelId) }
            }
        }
    }

    @Test
    fun `GET model by id returns 404 when model does not exist`() {
        val nonExistentId = 999L
        mockMvc.get("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `POST add model creates a new car model`() {
        val newModelJson = """
        {
            "id": 0,
            "brand": "Honda",
            "model": "Civic",
            "modelYear": 2021,
            "segment": "Compact",
            "doorsNo": 4,
            "seatingCapacity": 5,
            "luggageCapacity": 400.0,
            "manufacturer": "Honda",
            "costPerDay": 60.0,
            "motorDisplacement": 2000.0,
            "airConditioning": true,
            "category": "category1",
            "engine": "diesel",
            "transmission": "manual",
            "drivetrain": "AWD",
            "safetyFeatures": ["feature1", "feature3"],
            "infotainments": ["USB", "radio"]
        }
    """.trimIndent()

        mockMvc.post(SERVER_API) {
            contentType = MediaType.APPLICATION_JSON
            content = newModelJson
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { isNumber() }
            }
        }
    }

    @Test
    fun `PUT update model updates the car model when it exists`() {
        val carModelId = 1L
        val updatedModelJson = """
        {
            "id": $carModelId,
            "brand": "Toyota",
            "model": "Camry",
            "modelYear": 2022,
            "segment": "Sedan",
            "doorsNo": 4,
            "seatingCapacity": 5,
            "luggageCapacity": 450.0,
            "manufacturer": "Toyota",
            "costPerDay": 70.0,
            "motorDisplacement": 2500.0,
            "airConditioning": true,
            "category": "category2",
            "engine": "petrol",
            "transmission": "automatic",
            "drivetrain": "FWD",
            "safetyFeatures": ["feature2", "feature1"],
            "infotainments": ["USB", "radio"]
        }
    """.trimIndent()

        mockMvc.put("${SERVER_API}/$carModelId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedModelJson
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            // no content in the response

        }
    }

    @Test
    fun `PUT update model returns 404 when car model does not exist`() {
        val nonExistentId = 999L
        val updatedModelJson = """
        {
            "id": $nonExistentId,
            "brand": "Toyota",
            "model": "Camry",
            "modelYear": 2022,
            "segment": "Sedan",
            "doorsNo": 4,
            "seatingCapacity": 5,
            "luggageCapacity": 450.0,
            "manufacturer": "Toyota",
            "costPerDay": 70.0,
            "motorDisplacement": 2500.0,
            "airConditioning": true,
            "category": "Luxury",
            "engine": "Hybrid",
            "transmission": "Automatic",
            "drivetrain": "FWD",
            "safetyFeatures": ["Airbags", "Lane Assist"],
            "infotainments": ["Bluetooth", "Touchscreen"]
        }
    """.trimIndent()

        mockMvc.put("${SERVER_API}/$nonExistentId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedModelJson
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE model by id returns 404 when model does not exist`() {
        val nonExistentId = 999L
        mockMvc.delete("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE model by id deletes the model when it exists`() {
        val carModelId = 1L
        mockMvc.delete("${SERVER_API}/$carModelId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }
    }


    @Test
    fun `GET filters returns all available filters`() {
        mockMvc.get("${SERVER_API}/filters") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.categories") { isArray() }
                jsonPath("$.engines") { isArray() }
                jsonPath("$.transmissions") { isArray() }
                jsonPath("$.drivetrains") { isArray() }
                jsonPath("$.safetyFeatures") { isArray() }
                jsonPath("$.infotainments") { isArray() }
            }
        }
    }

 */