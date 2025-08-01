package it.wa2.reservationservice


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.wa2.reservationservice.advice.*
import it.wa2.reservationservice.controllers.ReservationController
import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.entities.Reservation
import it.wa2.reservationservice.entities.Status
import it.wa2.reservationservice.entities.Vehicle
import it.wa2.reservationservice.repositories.*
import it.wa2.reservationservice.services.ReservationService
import it.wa2.reservationservice.services.ReservationServiceImpl
import it.wa2.reservationservice.IntegrationTest
import it.wa2.reservationservice.dtos.StatusDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerMockMvcTest : IntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc
    @MockkBean
    lateinit var reservationService: ReservationService
    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createDTO(): ReservationDTO = ReservationDTO(
        id = 1,
        customerId = 1,
        employeeId = 2,
        vehicleId = 3,
        status = StatusDTO(1, "PENDING"),
        reservationDate = Date(),
        startDate = Date(System.currentTimeMillis()+86400000),
        endDate = Date(System.currentTimeMillis()+172800000)
    )

    @Test
    fun `GET reservations`() {
        val dto = createDTO()
        val page = PageImpl(listOf(dto))
        every { reservationService.getReservations(any(), any(), any(), any(), any(), any(), any(), any()) } returns page

        mockMvc.get("/api/v1/reservations?page=0&size=10") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].id") { value(dto.id) }
        }
    }

    @Test
    fun `GET by id not found`() {
        every { reservationService.getReservationById(99) } throws ReservationNotFound("x")
        mockMvc.get("/api/v1/reservations/99") { accept = MediaType.APPLICATION_JSON }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `POST addReservation returns created`() {
        val dto = createDTO()
        every { reservationService.addReservation(any()) } returns dto
        mockMvc.post("/api/v1/reservations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(dto)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(dto.id) }
        }
    }

    @Test
    fun `DELETE reservation`() {
        every { reservationService.deleteReservationById(1) } returns Unit
        mockMvc.delete("/api/v1/reservations/1") { accept = MediaType.APPLICATION_JSON }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `get all reservations returns 200 with page of reservations`() {
        val reservation = ReservationDTO(
            id = 1L,
            customerId = 1L,
            employeeId = 1L,
            vehicleId = 1L,
            status = StatusDTO(1L, "PENDING"),
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 172800000),
            paymentAmount = 100.0,
            version = 1L
        )
        val page = PageImpl(listOf(reservation))
        every {
            reservationService.getReservations(any(), any(), any(), any(), any(), any(), any(), any())
        } returns page

        mockMvc.get("/api/v1/reservations")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value(1) }
            }

        verify(exactly = 1) {
            reservationService.getReservations(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `get reservation by id returns 200 with reservation`() {
        val reservationId = 1L
        val reservation = ReservationDTO(
            id = reservationId,
            customerId = 1L,
            employeeId = 1L,
            vehicleId = 1L,
            status = StatusDTO(1L, "PENDING"),
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 172800000),
            paymentAmount = 100.0,
            version = 1L
        )
        every { reservationService.getReservationById(reservationId) } returns reservation

        mockMvc.get("/api/v1/reservations/$reservationId")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(reservationId) }
            }

        verify { reservationService.getReservationById(reservationId) }
    }

    @Test
    fun `add reservation returns 201 with location header`() {
        val reservation = """
            {
                "customerId": 1,
                "employeeId": 1,
                "vehicleId": 1,
                "status": {
                    "id": 1,
                    "status": "PENDING"
                },
                "reservationDate": "2025-05-22T12:43:04.172Z",
                "startDate": "2026-05-23T12:43:04.172Z",
                "endDate": "2026-05-24T12:43:04.172Z",
                "paymentAmount": 0,
                "version": 1
            }
        """.trimIndent()

        every { reservationService.addReservation(any()) } returns ReservationDTO(
            id = 1L,
            customerId = 1L,
            employeeId = 1L,
            vehicleId = 1L,
            status = StatusDTO(1L, "PENDING"),
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 172800000),
            paymentAmount = 100.0,
            version = 1L
        )

        mockMvc.post("/api/v1/reservations") {
            contentType = MediaType.APPLICATION_JSON
            content = reservation
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
        }

        verify { reservationService.addReservation(any()) }
    }

    @Test
    fun `update reservation returns 200`() {
        val reservationId = 1L
        val reservation = """
            {
                "id": 1,
                "customerId": 1,
                "employeeId": 1,
                "vehicleId": 1,
                "status": {
                    "id": 1,
                    "status": "COMPLETED"
                },
                "reservationDate": "2025-05-22T12:43:04.172Z",
                "startDate": "2026-05-23T12:43:04.172Z",
                "endDate": "2026-05-24T12:43:04.172Z",
                "paymentAmount": 0,
                "version": 1
            }
        """.trimIndent()
        every { reservationService.updateReservationById(eq(reservationId), any()) } returns
                ReservationDTO(
                    id = 1L,
                    customerId = 1L,
                    employeeId = 1L,
                    vehicleId = 1L,
                    status = StatusDTO(1L, "COMPLETED"),
                    reservationDate = Date(),
                    startDate = Date(System.currentTimeMillis() + 86400000),
                    endDate = Date(System.currentTimeMillis() + 172800000),
                    paymentAmount = 100.0,
                    version = 1L
                )

        mockMvc.put("/api/v1/reservations/$reservationId") {
            contentType = MediaType.APPLICATION_JSON
            content = reservation
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(reservationId) }
            jsonPath("$.status.status") { value("COMPLETED") }
        }

        verify { reservationService.updateReservationById(eq(reservationId), any()) }
    }

    @Test
    fun `delete reservation returns 200`() {
        val reservationId = 1L
        every { reservationService.deleteReservationById(reservationId) } returns Unit

        mockMvc.delete("/api/v1/reservations/$reservationId")
            .andExpect {
                status { isOk() }
            }

        verify { reservationService.deleteReservationById(reservationId) }
    }

    @Test
    fun `pay reservation returns 200 and message`() {
        val reservationId = 1L
        val message = "Payment successful"
        every { reservationService.payReservation(reservationId) } returns message

        mockMvc.post("/api/v1/reservations/$reservationId/pay")
            .andExpect {
                status { isOk() }
                content { string(message) }
            }

        verify { reservationService.payReservation(reservationId) }
    }

    @Test
    fun `get taken dates for a car model returns 200 with dates`() {
        val carModelId = 1L
        val dates = setOf(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 2))
        every { reservationService.getFullyBookedDates(carModelId) } returns dates

        mockMvc.get("/api/v1/reservations/$carModelId/takenDates")
            .andExpect {
                status { isOk() }
                jsonPath("$[0]") { value("2025-05-01") }
                jsonPath("$[1]") { value("2025-05-02") }
            }

        verify { reservationService.getFullyBookedDates(carModelId) }
    }
}