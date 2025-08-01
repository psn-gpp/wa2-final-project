package it.wa2.paymentservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.paymentservice.IntegrationTest
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.services.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import org.springframework.http.MediaType
import it.wa2.paymentservice.advices.PaymentCreationBadRequest
import it.wa2.paymentservice.advices.PaymentCreationInternalServerError
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerMockMvcTest: IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createPaymentOrder should return payment link`() {

        val requestDTO = PaymentOrderRequestDTO(
            reservationId = 1L,
            paymentAmount = 100.0
        )
        val expectedLink = "https://paypal.com/checkout/1234"

        every { paymentService.createPaymentOrder(any()) } returns expectedLink


        mockMvc.post("/api/v1/orders/create"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status { isOk() }
            content { string(expectedLink) }
        }
    }

    @Test
    fun `createPaymentOrder should handle invalid request`() {

        val invalidRequestDTO = PaymentOrderRequestDTO(
            reservationId = 1,
            paymentAmount = -100.0
        )

        every { paymentService.createPaymentOrder(any()) } throws PaymentCreationBadRequest("invalid amount")

        mockMvc.post("/api/v1/orders/create") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequestDTO)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `createPaymentOrder should handle service exception`() {

        val requestDTO = PaymentOrderRequestDTO(
            reservationId = 1L,
            paymentAmount = 100.0
        )

        every { paymentService.createPaymentOrder(requestDTO) } throws PaymentCreationInternalServerError("Error creating payment")


        mockMvc.post("/api/v1/orders/create") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status { isInternalServerError() }
        }
    }
}