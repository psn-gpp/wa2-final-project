package it.wa2.reservationservice

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.dtos.StatusDTO
import it.wa2.reservationservice.dtos.VehicleDTO
import it.wa2.reservationservice.entities.*
import it.wa2.reservationservice.services.PaymentClient
import it.wa2.reservationservice.services.UserClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import java.util.Date
import kotlin.test.assertEquals

@Disabled("Cannot figure out how to run tests with TestRestTemplate")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationRestTestTemplate (
    @Autowired val restTemplate: TestRestTemplate
) : IntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port/api/v1/reservations"

    @MockkBean
    lateinit var userClient: UserClient

    @MockkBean
    lateinit var paymentClient: PaymentClient


    private fun createTestReservation(statusValue: String): ReservationDTO {
        val carModel = CarModelDTO(
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

        val availability = Availability(id = 1L, type = "available")

        val vehicle = VehicleDTO(
            id = 12345678L,
            refCarModel = carModel.id,
            availability = "available",
            licencePlate = "AB123CD",
            vin = "KJDKSJK3K3KJJ4K44",
            kilometers = 100F,
            pendingCleaning = true,
            pendingMaintenance = false
        )

        val status = StatusDTO(id = 1, status = statusValue)

        return ReservationDTO(
            id = 1L,
            customerId = 1L,
            employeeId = 2L,
            vehicleId = vehicle.id,
            status = status,
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 2 * 86400000),
            paymentAmount = 100.0,
            version = 1L,
        )
    }

    @Test
    fun `GET reservations - should return 200 OK and empty list`() {

        val response = restTemplate.exchange(
            getBaseUrl(),
            HttpMethod.GET,
            null,
            String::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `POST reservation - should create a reservation`() {
        val dto = ReservationDTO(
            id = 999L,
            customerId = 1L,
            employeeId = 2L,
            vehicleId = 1L,
            status = StatusDTO(1L, "PENDING"),
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 2 * 86400000),
            paymentAmount = 200.0
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(dto, headers)

        every { userClient.getUserById(1L) } returns Unit

        val response = restTemplate.postForEntity(getBaseUrl(), request, String::class.java)
        println("RESPONSE BODY: ${response.body}")
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        //assertEquals(999L, response.body!!.id)
    }

    @Test
    fun `GET reservation by ID - should return reservation`() {
        val reservation = createTestReservation("APPROVED")

        val response = restTemplate.getForEntity("${getBaseUrl()}/10", ReservationDTO::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(10L, response.body!!.id)
    }

    @Test
    fun `DELETE reservation - should return 200 OK`() {
        val reservation = createTestReservation("APPROVED")

        val response = restTemplate.exchange(
            "${getBaseUrl()}/10",
            HttpMethod.DELETE,
            null,
            Void::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `POST payReservation - should return 200 with payment link`() {
        val reservation = createTestReservation("APPROVED")
        every { paymentClient.createPayPalOrder(any()) } returns "https://paypal.com/pay/10"

        val response = restTemplate.postForEntity(
            "${getBaseUrl()}/10/pay", null, String::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.contains("https://paypal.com/pay"))
    }

    @Test
    fun `POST payReservation - should return 403 if reservation not approved`() {
        val reservation = createTestReservation("PENDING")

        val response = restTemplate.postForEntity(
            "${getBaseUrl()}/10/pay", null, String::class.java
        )

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertTrue(response.body!!.contains("not yet approved", ignoreCase = true))
    }
}
