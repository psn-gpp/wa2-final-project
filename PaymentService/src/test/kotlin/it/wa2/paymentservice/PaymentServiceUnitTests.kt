package it.wa2.paymentservice

import com.ninjasquad.springmockk.MockkBean
import com.paypal.sdk.PaypalServerSdkClient
import com.paypal.sdk.controllers.OrdersController
import com.paypal.sdk.http.response.ApiResponse
import com.paypal.sdk.models.CaptureOrderInput
import com.paypal.sdk.models.LinkDescription
import com.paypal.sdk.models.Order
import com.paypal.sdk.models.TokenType
import io.mockk.*
import it.wa2.paymentservice.advices.PaymentCreationBadRequest
import it.wa2.paymentservice.advices.PaymentCreationInternalServerError
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.dtos.PaymentOrderResponseDTO
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.services.PaymentServiceImpl
import it.wa2.paymentservice.services.TransactionalPaymentOperations
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletResponse


//RUNNARE DOCKER

@SpringBootTest
class PaymentServiceUnitTests : IntegrationTest() {

	@MockkBean
	private lateinit var payPalClient: PaypalServerSdkClient

	@MockkBean
	private lateinit var transactionalPaymentOperations: TransactionalPaymentOperations

	@Autowired
	private lateinit var paymentService: PaymentServiceImpl

	@BeforeEach
	fun setup() {
		clearAllMocks()
	}
	/*@Autowired
	private lateinit var paymentService: PaymentServiceImpl

	@Autowired
	private lateinit var transactionalPaymentOperations: TransactionalPaymentOperations

	@Autowired
	lateinit var paymentRepository: PaymentRepository*/



	/*@Test
	fun `should create payment and return paypal link`() {
		val request = PaymentOrderRequestDTO(
			reservationId = 100,
			paymentAmount = 50.0
		)

		val link = paymentService.createPaymentOrder(request)

		// Verifica che il link sia una stringa valida (puoi fare check più precisi se vuoi)
		assertTrue(link.contains("http"), "Expected a valid PayPal URL")

		val payments = paymentRepository.findAll()
		Assertions.assertEquals(1, payments.size)
		Assertions.assertEquals(PaymentStatus.PENDING, payments[0].status)
	}*/

	/*########## createPaymentOrder test ##############*/
	@Test
	fun `createPaymentOrder throws PaymentCreationInternalServerError when createPaypalOrder returns null`() {
		// Given
		val paymentOrderRequestDTO = PaymentOrderRequestDTO(
			reservationId = 1L,
			paymentAmount = 100.0
		)

		val payment = Payment(
			reservationId = paymentOrderRequestDTO.reservationId,
			status = PaymentStatus.CREATED,
			paymentAmount = paymentOrderRequestDTO.paymentAmount,
		)

		// Mock transactionalPaymentOperations.addPayment
		every { transactionalPaymentOperations.addPayment(any()) } returns payment

		// Setup the controller mock chain for PayPal client - this time to simulate an exception
		val orderController = mockk<OrdersController>()
		every { payPalClient.ordersController } returns orderController
		every { orderController.createOrder(any()) } throws Exception("PayPal error")

		// When/Then
		assertThrows<PaymentCreationInternalServerError> {
			paymentService.createPaymentOrder(paymentOrderRequestDTO)
		}

		// Verify interactions
		verify { transactionalPaymentOperations.addPayment(any()) }
		verify { orderController.createOrder(any()) }
		verify(exactly = 0) { transactionalPaymentOperations.setInProgress(any(), any()) }
	}

	@Test
	fun `createPaymentOrder returns correct URL when all operations succeed`() {
		// Given
		val paymentOrderRequestDTO = PaymentOrderRequestDTO(
			reservationId = 1L,
			paymentAmount = 100.0
		)

		val payment = Payment(
			reservationId = paymentOrderRequestDTO.reservationId,
			status = PaymentStatus.CREATED,
			paymentAmount = paymentOrderRequestDTO.paymentAmount,
		)

		// Mock transactionalPaymentOperations.addPayment
		every { transactionalPaymentOperations.addPayment(any()) } returns payment

		// Mock createPaypalOrder
		val mockOrder = mockk<Order>()
		val mockLinks = listOf(
			mockk<LinkDescription>(),
			mockk<LinkDescription>()
		)
		every { mockLinks[0].href } returns "http://approve.url"
		every { mockLinks[1].href } returns "http://checkout.url"
		every { mockOrder.id } returns "ORDER123"
		every { mockOrder.links } returns mockLinks

		// Setup the controller mock chain for PayPal client
		val orderController = mockk<OrdersController>()
		every { payPalClient.ordersController } returns orderController

		val createOrderResponse = mockk<ApiResponse<Order>>()
		every { createOrderResponse.result } returns mockOrder
		every { orderController.createOrder(any()) } returns createOrderResponse

		// Mock setInProgress
		every { transactionalPaymentOperations.setInProgress(any(), any()) } just runs

		// When
		val result = paymentService.createPaymentOrder(paymentOrderRequestDTO)

		// Then
		Assertions.assertEquals("http://checkout.url", result)

		// Verify interactions
		verify { transactionalPaymentOperations.addPayment(any()) }
		verify { orderController.createOrder(any()) }
		verify { transactionalPaymentOperations.setInProgress(payment.id, "ORDER123") }
	}

	@Test
	fun `createPaymentOrder throws PaymentCreationBadRequest when amount is invalid`() {
		// Given
		val paymentOrderRequestDTO = PaymentOrderRequestDTO(
			reservationId = 1L,
			paymentAmount = 0.0
		)

		// When/Then
		assertThrows<PaymentCreationBadRequest> {
			paymentService.createPaymentOrder(paymentOrderRequestDTO)
		}

		// Verify no interactions with the mocks
		verify(exactly = 0) { transactionalPaymentOperations.addPayment(any()) }
		verify(exactly = 0) { payPalClient.ordersController }
	}

	@Test
	fun `createPaymentOrder throws PaymentCreationBadRequest when payment is duplicated`() {
		// Given
		val paymentOrderRequestDTO = PaymentOrderRequestDTO(
			reservationId = 1L,
			paymentAmount = 100.0
		)

		// Mock transactionalPaymentOperations.addPayment to return null (duplicated payment)
		every { transactionalPaymentOperations.addPayment(any()) } returns null

		// When/Then
		assertThrows<PaymentCreationBadRequest> {
			paymentService.createPaymentOrder(paymentOrderRequestDTO)
		}

		// Verify interactions
		verify { transactionalPaymentOperations.addPayment(any()) }
		verify(exactly = 0) { payPalClient.ordersController }
	}

	@Test
	fun `createPaymentOrder throws PaymentCreationInternalServerError when link is null`() {
		// Given
		val paymentOrderRequestDTO = PaymentOrderRequestDTO(
			reservationId = 1L,
			paymentAmount = 100.0
		)

		val payment = Payment(
			reservationId = paymentOrderRequestDTO.reservationId,
			status = PaymentStatus.CREATED,
			paymentAmount = paymentOrderRequestDTO.paymentAmount,
		)

		// Mock transactionalPaymentOperations.addPayment
		every { transactionalPaymentOperations.addPayment(any()) } returns payment

		// Mock createPaypalOrder
		val mockOrder = mockk<Order>()
		val mockLinks = listOf(
			mockk<LinkDescription>(),
			mockk<LinkDescription>()
		)
		every { mockLinks[0].href } returns "http://approve.url"
		every { mockLinks[1].href } returns null
		every { mockOrder.id } returns "ORDER123"
		every { mockOrder.links } returns mockLinks

		// Setup the controller mock chain for PayPal client
		val orderController = mockk<OrdersController>()
		every { payPalClient.ordersController } returns orderController

		val createOrderResponse = mockk<ApiResponse<Order>>()
		every { createOrderResponse.result } returns mockOrder
		every { orderController.createOrder(any()) } returns createOrderResponse

		// Mock setInProgress
		every { transactionalPaymentOperations.setInProgress(any(), any()) } just runs

		// When/Then
		assertThrows<PaymentCreationInternalServerError> {
			paymentService.createPaymentOrder(paymentOrderRequestDTO)
		}

		// Verify interactions
		verify { transactionalPaymentOperations.addPayment(any()) }
		verify { orderController.createOrder(any()) }
		verify { transactionalPaymentOperations.setInProgress(payment.id, "ORDER123") }
	}

	@Test
	fun `test createPaypalOrder returns null when PayPal client throws exception`() {
		// Given
		val payment = Payment(
			reservationId = 1L,
			status = PaymentStatus.CREATED,
			paymentAmount = 100.0
		)

		// Setup the controller mock chain for PayPal client to throw exception
		val orderController = mockk<OrdersController>()
		every { payPalClient.ordersController } returns orderController
		every { orderController.createOrder(any()) } throws Exception("PayPal API error")

		// When
		val result = paymentService.createPaypalOrder(payment)

		// Then
		Assertions.assertNull(result)
		verify { orderController.createOrder(any()) }
	}

	/*#####################################################*/

	/*########## capturePaymentOrder test ##############*/
	@Test
	fun `capturePaymentOrder success test`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = MockHttpServletResponse()

		// Mock dependencies
		every { transactionalPaymentOperations.setPaid(any()) } just runs
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } just runs

		// When
		val result = paymentService.capturePaymentOrder(token, mockResponse)

		// Then
		Assertions.assertEquals("http://localhost:8080/ui/loading", result)
		Assertions.assertEquals("http://localhost:8080/ui/loading", mockResponse.redirectedUrl)

		// Verify interactions
		verify { transactionalPaymentOperations.setPaid(token) }
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
	}

	@Test
	fun `capturePaymentOrder passes correct token to dependencies`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-ABC"
		val mockResponse = MockHttpServletResponse()

		// Capture actual parameters passed to mocked methods
		val setPaidTokenSlot = slot<String>()
		val createEventTokenSlot = slot<String>()

		every { transactionalPaymentOperations.setPaid(capture(setPaidTokenSlot)) } just runs
		every { transactionalPaymentOperations.createPayPalCaptureEvent(capture(createEventTokenSlot)) } just runs

		// When
		paymentService.capturePaymentOrder(token, mockResponse)

		// Then
		Assertions.assertEquals(token, setPaidTokenSlot.captured)
		Assertions.assertEquals(token, createEventTokenSlot.captured)
	}

	@Test
	fun `capturePaymentOrder when setPaid throws exception`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = MockHttpServletResponse()

		// Mock setPaid to throw an exception
		every { transactionalPaymentOperations.setPaid(any()) } throws RuntimeException("Database error")
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } just runs

		// When/Then
		Assertions.assertThrows(RuntimeException::class.java) {
			paymentService.capturePaymentOrder(token, mockResponse)
		}

		// Verify interactions - setPaid was called but createPayPalCaptureEvent was not
		verify { transactionalPaymentOperations.setPaid(token) }
		verify(exactly = 0) { transactionalPaymentOperations.createPayPalCaptureEvent(any()) }
	}

	@Test
	fun `capturePaymentOrder when createPayPalCaptureEvent throws exception`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = MockHttpServletResponse()

		// Mock setPaid to succeed but createPayPalCaptureEvent to throw an exception
		every { transactionalPaymentOperations.setPaid(any()) } just runs
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } throws
				RuntimeException("Failed to create event")

		// When/Then
		Assertions.assertThrows(RuntimeException::class.java) {
			paymentService.capturePaymentOrder(token, mockResponse)
		}

		// Verify interactions
		verify { transactionalPaymentOperations.setPaid(token) }
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
	}

	@Test
	fun `capturePaymentOrder when sendRedirect throws IOException`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = mockk<HttpServletResponse>()

		// Mock dependencies
		every { transactionalPaymentOperations.setPaid(any()) } just runs
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } just runs
		every { mockResponse.sendRedirect(any()) } throws java.io.IOException("Redirect failed")

		// When/Then
		Assertions.assertThrows(java.io.IOException::class.java) {
			paymentService.capturePaymentOrder(token, mockResponse)
		}

		// Verify that the transactional operations completed
		verify { transactionalPaymentOperations.setPaid(token) }
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
		verify { mockResponse.sendRedirect("http://localhost:8080/ui/loading") }
	}

	/*#####################################################*/

	/*########## capturePaymentOrder test ##############*/
	@Test
	fun `cancelPaymentOrder success test`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = MockHttpServletResponse()

		// Mock dependencies
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } just runs

		// When
		val result = paymentService.cancelPaymentOrder(token, mockResponse)

		// Then
		Assertions.assertEquals("http://localhost:8080/ui/loading?cancel=true", result)
		Assertions.assertEquals("http://localhost:8080/ui/loading?cancel=true", mockResponse.redirectedUrl)

		// Verify interactions
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
	}

	@Test
	fun `cancelPaymentOrder passes correct token to dependencies`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-ABC"
		val mockResponse = MockHttpServletResponse()

		// Capture actual parameters passed to mocked methods
		val createEventTokenSlot = slot<String>()

		every { transactionalPaymentOperations.createPayPalCaptureEvent(capture(createEventTokenSlot)) } just runs

		// When
		paymentService.cancelPaymentOrder(token, mockResponse)

		// Then
		Assertions.assertEquals(token, createEventTokenSlot.captured)
	}

	@Test
	fun `cancelPaymentOrder when createPayPalCaptureEvent throws exception`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = MockHttpServletResponse()

		// Mock setPaid to succeed but createPayPalCaptureEvent to throw an exception
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } throws
				RuntimeException("Failed to create event")

		// When/Then
		Assertions.assertThrows(RuntimeException::class.java) {
			paymentService.cancelPaymentOrder(token, mockResponse)
		}

		// Verify interactions
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
	}

	@Test
	fun `cancelPaymentOrder when sendRedirect throws IOException`() {
		// Given
		val token = "PAYPAL-ORDER-TOKEN-123"
		val mockResponse = mockk<HttpServletResponse>()

		// Mock dependencies
		every { transactionalPaymentOperations.createPayPalCaptureEvent(any()) } just runs
		every { mockResponse.sendRedirect(any()) } throws java.io.IOException("Redirect failed")

		// When/Then
		Assertions.assertThrows(java.io.IOException::class.java) {
			paymentService.cancelPaymentOrder(token, mockResponse)
		}

		// Verify that the transactional operations completed
		verify { transactionalPaymentOperations.createPayPalCaptureEvent(token) }
		verify { mockResponse.sendRedirect("http://localhost:8080/ui/loading?cancel=true") }
	}

	/*#####################################################*/

	/*########## getPaymentOrder test ##############*/
	@Test
	fun `getPaymentOrder success test`(){
		//Given
		val paypalToken="PAYPAL_TOKEN"
		val payment = Payment(
			paypalOrderId = paypalToken,
			reservationId = 100,
			status = PaymentStatus.PENDING,
			paymentAmount = 50.0,
		)

		val resultDTO = PaymentOrderResponseDTO(
			reservationId = payment.reservationId,
			paypalOrderId = paypalToken,
			status = payment.status,
			message = "Payment found for token PAYPAL_TOKEN",
			error = null
		)

		// Mock dependencies
		every { transactionalPaymentOperations.getPayment(paypalToken) } returns payment

		val result = paymentService.getPaymentOrder(paypalToken)

		Assertions.assertEquals(resultDTO, result)


	}

	/*TODO : manage notFound error*/

	/*#####################################################*/

	/*########## captureOrder test ##############*/

	/*
	@Test
	fun `captureOrder successfully captures PayPal order`() {
		// Given
		val token = "TEST-ORDER-TOKEN-123"

		// Mock PayPal client response
		val mockOrder = mockk<Order>()
		val ordersController = mockk<OrdersController>()
		val captureResponse = mockk<ApiResponse<Order>>()

		every { payPalClient.ordersController } returns ordersController
		every { captureResponse.result } returns mockOrder

		// Capture the input to verify it was constructed correctly
		val captureInputSlot = slot<CaptureOrderInput>()
		every { ordersController.captureOrder(capture(captureInputSlot)) } returns captureResponse

		// When
		val result = paymentService.captureOrder(token)

		// Then
		Assertions.assertEquals(mockOrder, result)

		// Verify the CaptureOrderInput was constructed correctly
		val capturedInput = captureInputSlot.captured
		Assertions.assertEquals(token, capturedInput.id)

		// Verify the request body was constructed correctly
		val requestBody = capturedInput.body
		Assertions.assertNotNull(requestBody.paymentSource)

		// Verify token was set correctly in the payment source
		val paymentSource = requestBody.paymentSource
		Assertions.assertEquals(token, paymentSource.token.id)
		Assertions.assertEquals(TokenType.BILLING_AGREEMENT, paymentSource.token.type)

		// Verify interactions
		verify { payPalClient.ordersController }
		verify { ordersController.captureOrder(any()) }
	}


	 */
	@Test
	fun `captureOrder throws exception when PayPal client fails`() {
		// Given
		val token = "TEST-ORDER-TOKEN-123"
		val ordersController = mockk<OrdersController>()

		// Mock PayPal client to throw an exception
		every { payPalClient.ordersController } returns ordersController
		every { ordersController.captureOrder(any()) } throws Exception("PayPal API Error")

		// When/Then
		Assertions.assertThrows(Exception::class.java) {
			paymentService.captureOrder(token)
		}

		// Verify interactions
		verify { payPalClient.ordersController }
		verify { ordersController.captureOrder(any()) }
	}

	@Test
	fun `captureOrder handles case when PayPal returns null result`() {
		// Given
		val token = "TEST-ORDER-TOKEN-123"
		val ordersController = mockk<OrdersController>()
		val captureResponse = mockk<ApiResponse<Order>>()

		// Mock PayPal client to return null result
		every { payPalClient.ordersController } returns ordersController
		every { captureResponse.result } returns null
		every { ordersController.captureOrder(any()) } returns captureResponse

		// When/Then
		Assertions.assertThrows(NullPointerException::class.java) {
			paymentService.captureOrder(token)
		}

		// Verify interactions
		verify { payPalClient.ordersController }
		verify { ordersController.captureOrder(any()) }
		verify { captureResponse.result }
	}

	/*
	@Test
	fun `captureOrder with different token types`() {
		// Given
		val token = "TEST-ORDER-TOKEN-456"

		// Mock PayPal client response
		val mockOrder = mockk<Order>()
		val ordersController = mockk<OrdersController>()
		val captureResponse = mockk<ApiResponse<Order>>()

		every { payPalClient.ordersController } returns ordersController
		every { captureResponse.result } returns mockOrder
		every { ordersController.captureOrder(any()) } returns captureResponse

		// When
		val result = paymentService.captureOrder(token)

		// Then
		Assertions.assertEquals(mockOrder, result)

		// Verify interactions
		verify { payPalClient.ordersController }
		verify { ordersController.captureOrder(any()) }
	}

	 */

}
