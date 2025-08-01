package it.wa2.reservationservice

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.reservationservice.controllers.VehicleNotFound
import it.wa2.reservationservice.dtos.VehicleDTO
import it.wa2.reservationservice.services.VehicleService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hibernate.query.Page.page
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.Commit
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

// ########################### VEHICLES #######################################


@SpringBootTest
@AutoConfigureMockMvc
class TestVehiclesController : IntegrationTest() {

    @Serializable
    data class Vehicle(
        val refCarModel: Long,
        val availability: String,
        val licencePlate: String,
        val vin: String,
        val kilometers: Float,
        val pendingCleaning: Boolean,
        val pendingMaintenance: Boolean
    )

    private val logger = LoggerFactory.getLogger(TestVehiclesController::class.java)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var vehicleService: VehicleService

    companion object {

    val SERVER_API = "/api/v1/vehicles"
    var carModelId = 1L
    val newModelJson = """
        {
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

        val newVehicle = Vehicle(
            refCarModel = carModelId,
            availability = "available",
            licencePlate = "AB123CD",
            vin = "KJDKSJK3K3KJJ4K44",
            kilometers = 100F,
            pendingCleaning = true,
            pendingMaintenance = false
        )

        val newVehicleDTO = VehicleDTO(
            id = 12345678L,
            refCarModel = carModelId,
            availability = "available",
            licencePlate = "AB123CD",
            vin = "KJDKSJK3K3KJJ4K44",
            kilometers = 100F,
            pendingCleaning = true,
            pendingMaintenance = false
        )
    }


    @Rollback(value = false)
    fun `ADD NEW MODEL`() {
        val newModelJson = """
        {
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

        mockMvc.post("/api/v1/models") {
            contentType = MediaType.APPLICATION_JSON
            content = newModelJson
        }
    }


    @Test
    @Commit
    @Transactional
    fun `POST add vehicle creates a new vehicle`() {
        // `ADD NEW MODEL`()


        println("Car model ID agagag: $carModelId")

        val newVehicleJson = """
            {
                "id" : 12345678,
                "refCarModel" : ${carModelId},
                "availability" : "available",
                "licencePlate" : "AB123CD",
                "vin" : "KJDKSJK3K3KJJ4K44",
                "kilometers" : 100.0,
                "pendingCleaning" : true,
                "pendingMaintenance" : false
            }
        """.trimIndent()

        every { vehicleService.addVehicle(any()) } returns newVehicleDTO
/*
        val newVehicleJson = Json.encodeToJsonElement(Vehicle.serializer(), newVehicle)
*/

        val response = mockMvc.post(SERVER_API) {
            contentType = MediaType.APPLICATION_JSON
            content = newVehicleJson
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { isNumber() }
                // check if response contains the new vehicle
                jsonPath("$.id") {  value(12345678) }
            }
        }

        println(response.andReturn().response.contentAsString)
    }

    @Test
    @Transactional
    @Commit
    fun `GET vehicles returns paginated results`() {
        val pageContent = listOf(newVehicleDTO)
        val page = PageImpl(pageContent)

        every { vehicleService.getVehicles(any(), any(), any(), any(), any(), any(), any(), any()) } returns page

        mockMvc.get("${SERVER_API}?page=0&size=10") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.content") { isArray() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].id") { value(newVehicleDTO.id) }
                jsonPath("$.content[0].licencePlate") { value(newVehicleDTO.licencePlate) }
            }
        }
    }

    @Test
    @Transactional
    @Commit
    fun `GET vehicle by id returns vehicle when it exists`() {
        //`ADD NEW MODEL`()
        //`POST add vehicle creates a new vehicle`()

        every { vehicleService.getVehicleById(any()) } returns newVehicleDTO

        val vehicleId = 1L
        mockMvc.get("${SERVER_API}/${newVehicleDTO.id}") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { value(newVehicleDTO.id) }
            }
        }
    }

    @Test
    fun `GET vehicle by id returns 404 when vehicle does not exist`() {
        val nonExistentId = 999L

        every { vehicleService.getVehicleById(nonExistentId) } throws VehicleNotFound("Vehicle not found")

        mockMvc.get("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content {
                contentType(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.title") { value("Vehicle not found") }
        }

    }

    @Test
    @Transactional
    @Commit
    fun `PUT update vehicle updates the vehicle when it exists`() {
        /*`ADD NEW MODEL`()
        `POST add vehicle creates a new vehicle`()*/

        val vehicleId = 1L
        val updatedVehicle = VehicleDTO(
            id = vehicleId,
            refCarModel = carModelId,
            availability = "rented",
            licencePlate = "WX456YZ",
            vin = "KJDKSJK3K3KJJ4K44",
            kilometers = 200F,
            pendingCleaning = false,
            pendingMaintenance = true
        )

        every { vehicleService.modifyVehicle(any(), any()) } returns Unit


        val updatedVehicleJson = Json.encodeToJsonElement(VehicleDTO.serializer(), updatedVehicle)

        mockMvc.put("${SERVER_API}/$vehicleId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedVehicleJson
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        every { vehicleService.getVehicleById(any()) } returns updatedVehicle

        mockMvc.get("${SERVER_API}/${updatedVehicle.id}") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.id") { value(updatedVehicle.id) }
                jsonPath("$.availability") { value(updatedVehicle.availability) }
                jsonPath("$.licencePlate") { value(updatedVehicle.licencePlate) }
                jsonPath("$.vin") { value(updatedVehicle.vin) }
                jsonPath("$.kilometers") { value(updatedVehicle.kilometers) }
                jsonPath("$.pendingCleaning") { value(updatedVehicle.pendingCleaning) }
                jsonPath("$.pendingMaintenance") { value(updatedVehicle.pendingMaintenance) }
            }
        }
    }

   @Test
    fun `PUT update vehicle returns 404 when vehicle does not exist`() {
        val nonExistentId = 999L
        val updatedVehicle = VehicleDTO(
            id = nonExistentId,
            refCarModel = carModelId,
            availability = "rented",
            licencePlate = "WX456YZ",
            vin = "KJDKSJK3K3KJJ4K44",
            kilometers = 200F,
            pendingCleaning = false,
            pendingMaintenance = true
        )

        every { vehicleService.modifyVehicle(nonExistentId, any()) } throws VehicleNotFound("Vehicle not found")

        val updatedVehicleJson = Json.encodeToJsonElement(VehicleDTO.serializer(), updatedVehicle)

        mockMvc.put("${SERVER_API}/$nonExistentId") {
            contentType = MediaType.APPLICATION_JSON
            content = updatedVehicleJson
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }
    }

    @Test
    fun `PATCH vehicle by id returns 404 when vehicle does not exist`() {
        val nonExistentId = 999L
        val patchJson = """
        [
            { "op": "replace", "path": "/availability", "value": "available" }
        ]
    """.trimIndent()

        every {
            vehicleService.patchVehicleById(
                nonExistentId,
                any()
            )
        } throws VehicleNotFound("Vehicle not found")

        mockMvc.patch("${SERVER_API}/$nonExistentId") {
            contentType = MediaType.parseMediaType("application/json-patch+json")
            content = patchJson
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Vehicle not found") }
        }
    }

    @Test
    fun `PATCH update vehicle status returns 404 when vehicle does not exist`() {
        val nonExistentId = 999L
        val statusUpdateJson = """
        [
            { "op": "replace", "path": "/availability", "value": "available" }
        ]
    """.trimIndent()

        every {
            vehicleService.patchVehicleById(
                nonExistentId,
                any()
            )
        } throws VehicleNotFound("Vehicle not found")

        mockMvc.patch("${SERVER_API}/$nonExistentId") {
            contentType = MediaType.parseMediaType("application/json-patch+json")
            content = statusUpdateJson
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Vehicle not found") }
        }
    }

    @Test
    @Transactional
    @Commit
    fun `GET vehicles by status returns vehicles with the specified status`() {
        val status = "available"
        val vehicleList = listOf(newVehicleDTO.copy(availability = status))
        val vehiclePage = PageImpl(vehicleList)

        every { vehicleService.getVehicles(any(), null, status, null, null, null, null, null) } returns vehiclePage

        mockMvc.get("${SERVER_API}?availability=$status") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.content") { isArray() }
                jsonPath("$.content[0].availability") { value(status) }
            }
        }
    }

    @Test
    @Transactional
    @Commit
    fun `DELETE vehicle by id deletes the vehicle when it exists`() {
        every { vehicleService.deleteVehicleById(any()) } returns Unit

        val vehicleId = 1L
        mockMvc.delete("${SERVER_API}/$vehicleId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        every { vehicleService.getVehicleById(vehicleId) } throws VehicleNotFound("Vehicle not found")

        mockMvc.get("${SERVER_API}/$vehicleId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Vehicle not found") }
        }
    }

    @Test
    fun `DELETE vehicle by id returns 404 when vehicle does not exist`() {
        val nonExistentId = 999L
        every { vehicleService.deleteVehicleById(nonExistentId) } throws VehicleNotFound("Vehicle not found")

        mockMvc.delete("${SERVER_API}/$nonExistentId") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Vehicle not found") }
        }
    }


}