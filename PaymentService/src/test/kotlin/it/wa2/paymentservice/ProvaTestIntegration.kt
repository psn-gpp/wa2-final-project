package it.wa2.paymentservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.paypal.sdk.PaypalServerSdkClient
import com.paypal.sdk.controllers.OrdersController
import com.paypal.sdk.http.response.ApiResponse
import com.paypal.sdk.models.*
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.repositories.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.hamcrest.Matchers.containsString
import org.awaitility.Awaitility.await
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = ["classpath:init_outbox_table.sql"])
class PaymentControllerIntegrationTest : IntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var paymentRepository: PaymentRepository


    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var payPalClient: PaypalServerSdkClient


    @Test
    fun `crea ordine PayPal e verifica risposta controller e stato db`() {

        val fakeOrder = Order().apply {
            id = "fake-paypal-id1"
            status = OrderStatus.CREATED
            links = listOf(
                LinkDescription().apply { href = "https://paypal.com/checkoutnow?token=fake-paypal-id1" },
                LinkDescription().apply { href = "https://paypal.com/checkoutnow?token=fake-paypal-id1" }
            )
        }

        val fakeApiResponse = mockk<ApiResponse<Order>>()
        every { fakeApiResponse.result } returns fakeOrder

        val fakeCaptureOrder = Order().apply {
            id = "fake-paypal-id"
            status = OrderStatus.COMPLETED
        }
        val fakeCaptureApiResponse = mockk<ApiResponse<Order>>()
        every { fakeCaptureApiResponse.result } returns fakeCaptureOrder

        val ordersControllerMock = mockk<OrdersController>()
        every { ordersControllerMock.createOrder(any()) } returns fakeApiResponse
        every { ordersControllerMock.captureOrder(any()) } returns fakeCaptureApiResponse

        every { payPalClient.ordersController } returns ordersControllerMock

        val request = PaymentOrderRequestDTO(
            reservationId = 123L,
            paymentAmount = 15.50
        )

        val response = mockMvc.post("/api/v1/orders/create") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        response.andExpect {
            status { isOk() }
            content { string(containsString("paypal.com/checkoutnow?token=fake-paypal-id1")) }
        }.andReturn()

        var payment = paymentRepository.findByReservationId(123L)
        assertNotNull(payment)
        assertEquals(PaymentStatus.PENDING, payment?.status)
        assertEquals(15.50, payment?.paymentAmount)
        assertEquals("fake-paypal-id1", payment?.paypalOrderId)


        println("PROVA: $response")

        mockMvc.get("/api/v1/orders/capture?token=fake-paypal-id1")
            .andExpect {
                status { isFound() }
                header { string("Location", "http://localhost:8080/ui/loading") }
            }


        await().atMost(30, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("fake-paypal-id1").status == PaymentStatus.COMPLETED
        }


        payment = paymentRepository.findByPaypalOrderId("fake-paypal-id1")
        println("PAYMENT: $payment")
        assertNotNull(payment)
        assertEquals(PaymentStatus.COMPLETED, payment.status)

    }


    @Test
    fun `crea ordine PayPal e cancel`() {

        val fakeOrder = Order().apply {
            id = "fake-paypal-id2"
            status = OrderStatus.CREATED
            links = listOf(
                LinkDescription().apply { href = "https://paypal.com/checkoutnow?token=fake-paypal-id2" },
                LinkDescription().apply { href = "https://paypal.com/checkoutnow?token=fake-paypal-id2" }
            )
        }

        val fakeApiResponse = mockk<ApiResponse<Order>>()
        every { fakeApiResponse.result } returns fakeOrder

        val fakeCaptureOrder = Order().apply {
            id = "fake-paypal-id2"
            status = OrderStatus.COMPLETED
        }
        val fakeCaptureApiResponse = mockk<ApiResponse<Order>>()
        every { fakeCaptureApiResponse.result } returns fakeCaptureOrder

        val ordersControllerMock = mockk<OrdersController>()
        every { ordersControllerMock.createOrder(any()) } returns fakeApiResponse
        every { ordersControllerMock.captureOrder(any()) } returns fakeCaptureApiResponse

        every { payPalClient.ordersController } returns ordersControllerMock

        val request = PaymentOrderRequestDTO(
            reservationId = 124L,
            paymentAmount = 15.50
        )

        val response = mockMvc.post("/api/v1/orders/create") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        response.andExpect {
            status { isOk() }
            content { string(containsString("paypal.com/checkoutnow?token=fake-paypal-id2")) }
        }.andReturn()

        var payment = paymentRepository.findByReservationId(124L)
        assertNotNull(payment)
        assertEquals(PaymentStatus.PENDING, payment?.status)
        assertEquals(15.50, payment?.paymentAmount)
        assertEquals("fake-paypal-id2", payment?.paypalOrderId)




        mockMvc.get("/api/v1/orders/cancel?token=fake-paypal-id2")
            .andExpect {
                status { isFound() }
                header { string("Location", "http://localhost:8080/ui/loading?cancel=true") }
            }


        await().atMost(30, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("fake-paypal-id2").status == PaymentStatus.CANCELLED
        }


        payment = paymentRepository.findByPaypalOrderId("fake-paypal-id2")
        println("PAYMENT: $payment")
        assertNotNull(payment)
        assertEquals(PaymentStatus.CANCELLED, payment.status)

    }



}