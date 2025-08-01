package it.wa2.reservationservice

import it.wa2.reservationservice.dtos.CarModelDTO
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Disabled("Cannot figure out how to run tests with TestRestTemplate")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CarModelRestTemplateTests : IntegrationTest() {
    @Autowired
    private lateinit var restTemplate : TestRestTemplate

    companion object {
        const val SERVER_URL = "http://localhost:8080/api/v1/models"
        var carModelId: Long? = null
    }


    @Test
    fun `log database state`() {
        val response = restTemplate.getForEntity("/api/v1/models", String::class.java)
        println("Database state: ${response.body}")
    }

    @Test
    fun `GET models empty`() {
        val response = restTemplate.getForEntity("/api/v1/models", String::class.java)
        assert(response.statusCode.is2xxSuccessful)
        assert(response.body?.contains("content") == true)
        assert(response.body?.contains("[]") == true)
    }

    @Test
    @Order(1)
    fun `POST add CarModel`() {
        val newCarModel = CarModelDTO(
            id = 0L,
            brand = "Toyota",
            model = "Corolla",
            modelYear = 2023,
            segment = "Sedan",
            doorsNo = 5,
            seatingCapacity = 5,
            luggageCapacity = 340.0f,
            category = "category1",
            manufacturer = "Toyota Manufacturer",
            costPerDay = 100.0,
            motorDisplacement = 1500.0f,
            airConditioning = true,
            engine = "petrol",
            transmission = "manual",
            drivetrain = "FWD",
            safetyFeatures = mutableListOf("Airbags", "Lane Assist"),
            infotainments = mutableListOf("Bluetooth", "USB")
        )

        val response = restTemplate.postForEntity(SERVER_URL, newCarModel, CarModelDTO::class.java)

        assert(response.statusCode == HttpStatus.CREATED)
        assert(response.body?.id != null)

        carModelId = response.body!!.id
    }

    @Test
    @Order(2)
    fun `GET CarModel by id`() {
        val response = restTemplate.getForEntity("$SERVER_URL/$carModelId", CarModelDTO::class.java)

        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.id == carModelId)
        assert(response.body?.brand == "Toyota")
    }

    @Test
    @Order(3)
    fun `PUT update CarModel`() {
        val updatedCarModel = CarModelDTO(
            id = carModelId?: 1L,
            brand = "Updated Brand",
            model = "Updated Model",
            modelYear = 2024,
            segment = "Hatchback",
            doorsNo = 3,
            seatingCapacity = 2,
            luggageCapacity = 200.0f,
            category = "category2",
            manufacturer = "Updated Manufacturer",
            costPerDay = 70.0,
            motorDisplacement = 2000.0f,
            airConditioning = false,
            engine = "electric",
            transmission = "automatic",
            drivetrain = "RWD",
            safetyFeatures = mutableListOf("ABS", "Traction Control"),
            infotainments = mutableListOf("Touchscreen", "Apple CarPlay")
        )

        restTemplate.put("$SERVER_URL/$carModelId", updatedCarModel)

        val response = restTemplate.getForEntity("$SERVER_URL/$carModelId", CarModelDTO::class.java)

        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.brand == "Updated Brand")
        assert(response.body?.drivetrain == "RWD")
    }

    @Test
    @Order(4)
    fun `GET all CarModels`() {
        val response: ResponseEntity<String> = restTemplate.getForEntity(SERVER_URL + "?page=0&size=10", String::class.java)

        assert(response.statusCode == HttpStatus.OK)
        assert(response.body!!.contains("content"))
    }

    @Test
    @Order(5)
    fun `GET CarModel filters`() {
        val response = restTemplate.getForEntity("$SERVER_URL/filters", Any::class.java)

        assert(response.statusCode == HttpStatus.OK)
        assert(response.body != null)
    }

    @Test
    @Order(6)
    fun `DELETE CarModel`() {
        restTemplate.delete("$SERVER_URL/$carModelId")

        val response = restTemplate.getForEntity("$SERVER_URL/$carModelId", String::class.java)
        assert(response.statusCode == HttpStatus.NOT_FOUND)
    }

}