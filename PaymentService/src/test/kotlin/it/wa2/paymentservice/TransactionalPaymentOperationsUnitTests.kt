package it.wa2.paymentservice

import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import it.wa2.paymentservice.advices.PaymentNotFoundException
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.entities.PaypalOutboxEvent
import it.wa2.paymentservice.repositories.OutboxEventRepository
import it.wa2.paymentservice.repositories.PaymentRepository
import it.wa2.paymentservice.services.TransactionalPaymentOperations
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.Test

@SpringBootTest
class TransactionalPaymentOperationsUnitTests : IntegrationTest() {
    @MockkBean
    private lateinit var paymentRepository: PaymentRepository

    @MockkBean
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var transactionalPaymentOperations: TransactionalPaymentOperations


    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    /*################ addPayment unit test #######################*/

    @Test
    fun `addPayment successfully saves new payment`() {
        // Given
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED, // This will be overwritten by addPayment
            paymentAmount = 100.0,
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByReservationId(payment.reservationId) } returns null

        // Mock save to return the saved payment
        val savedPayment = Payment( //copy of the starting value
            reservationId = 123L,
            status = PaymentStatus.CREATED, // This will be overwritten by addPayment
            paymentAmount = 100.0,
        )
        every { paymentRepository.save(any()) } returns savedPayment

        // When
        val result = transactionalPaymentOperations.addPayment(payment)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(payment.reservationId, result!!.reservationId)
        Assertions.assertEquals(PaymentStatus.CREATED, result.status)
        Assertions.assertEquals(payment.paymentAmount, result.paymentAmount)

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment.reservationId) }
        verify { paymentRepository.save(payment) }

        // Verify that status was set correctly before saving
        Assertions.assertEquals(PaymentStatus.CREATED, payment.status)
    }

    @Test
    fun `addPayment returns null when existing payment is COMPLETED`() {
        // Given
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED,
            paymentAmount = 100.0,
        )

        // Mock repository to return an existing COMPLETED payment
        val existingPayment = Payment( //copy of the starting value with status changed
            reservationId = 123L,
            status = PaymentStatus.COMPLETED,
            paymentAmount = 100.0,
        )
        every { paymentRepository.findByReservationId(payment.reservationId) } returns existingPayment

        // When
        val result = transactionalPaymentOperations.addPayment(payment)

        // Then
        Assertions.assertNull(result)

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment.reservationId) }
        verify(exactly = 0) { paymentRepository.save(any()) } // Ensure save was not called
    }

    @Test
    fun `addPayment saves payment when existing payment is not COMPLETED`() {
        // Given
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED,
            paymentAmount = 100.0,
        )

        // Mock repository to return an existing payment with non-COMPLETED status
        val existingPayment = Payment( //copy of the starting value with status changed
            reservationId = 123L,
            status = PaymentStatus.PENDING,
            paymentAmount = 100.0,
        )
        every { paymentRepository.findByReservationId(payment.reservationId) } returns existingPayment

        // Mock save to return the saved payment
        val savedPayment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED,
            paymentAmount = 100.0,
        )
        every { paymentRepository.save(any()) } returns savedPayment

        // When
        val result = transactionalPaymentOperations.addPayment(payment)

        // Then
        Assertions.assertNotNull(result)

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment.reservationId) }
        verify { paymentRepository.save(payment) }
    }

    @Test
    fun `addPayment overrides status even when already set`() {
        // Given
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.PENDING, // Set to something other than CREATED
            paymentAmount = 100.0,
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByReservationId(payment.reservationId) } returns null

        // Mock save to return the saved payment
        val savedPayment = Payment(
            reservationId = 123L,
            status = PaymentStatus.PENDING,
            paymentAmount = 100.0,
        )
        every { paymentRepository.save(any()) } returns savedPayment

        // When
        val result = transactionalPaymentOperations.addPayment(payment)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(PaymentStatus.CREATED, payment.status) // Status should be overridden

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment.reservationId) }
        verify { paymentRepository.save(payment) }
    }

    @Test
    fun `addPayment handles database exception`() {
        // Given
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED,
            paymentAmount = 100.0,
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByReservationId(payment.reservationId) } returns null

        // Mock save to throw an exception
        every { paymentRepository.save(any()) } throws RuntimeException("Database error")

        // When/Then
        Assertions.assertThrows(RuntimeException::class.java) {
            transactionalPaymentOperations.addPayment(payment)
        }

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment.reservationId) }
        verify { paymentRepository.save(payment) }

        // Verify that status was set correctly before attempting to save
        Assertions.assertEquals(PaymentStatus.CREATED, payment.status)
    }

    @Test
    fun `addPayment with multiple invocations`() {
        // Given
        val payment1 = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED,
            paymentAmount = 100.0,
        )

        val payment2 = Payment(
            reservationId = 456L,
            status = PaymentStatus.CREATED,
            paymentAmount = 200.0,
        )

        // Mock repository for first payment
        every { paymentRepository.findByReservationId(payment1.reservationId) } returns null
        every { paymentRepository.save(payment1) } returns payment1

        // Mock repository for second payment
        every { paymentRepository.findByReservationId(payment2.reservationId) } returns null
        every { paymentRepository.save(payment2) } returns payment2

        // When
        val result1 = transactionalPaymentOperations.addPayment(payment1)
        val result2 = transactionalPaymentOperations.addPayment(payment2)

        // Then
        Assertions.assertNotNull(result1)
        Assertions.assertNotNull(result2)
        Assertions.assertEquals(payment1.reservationId, result1!!.reservationId)
        Assertions.assertEquals(payment2.reservationId, result2!!.reservationId)

        // Verify interactions
        verify { paymentRepository.findByReservationId(payment1.reservationId) }
        verify { paymentRepository.save(payment1) }
        verify { paymentRepository.findByReservationId(payment2.reservationId) }
        verify { paymentRepository.save(payment2) }
    }

    /*###############################################################*/

    /*################ addPayment unit test #######################*/
    @Test
    fun `setInProgress successfully set status from CREATED TO PENDING`() {
        // Given
        val paymentId=1L
        val paypalId="TOKEN"
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CREATED, // This will be overwritten by addPayment
            paymentAmount = 100.0,
            paypalOrderId = ""
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByIdOrNull(paymentId) } returns payment


        transactionalPaymentOperations.setInProgress(paymentId, paypalId)

        // Then
        Assertions.assertEquals(payment.reservationId, 123L)
        Assertions.assertEquals(PaymentStatus.PENDING, payment.status)
        Assertions.assertEquals(100.0, payment.paymentAmount)
        Assertions.assertEquals(paypalId, "TOKEN")

        // Verify interactions
        verify { paymentRepository.findByIdOrNull(paymentId) }

    }

    @Test
    fun `setInProgress throw PaymentNotFoundException`() {
        // Given
        val paymentId=1L
        val paypalId="TOKEN"

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByIdOrNull(paymentId) } returns null

        Assertions.assertThrows(PaymentNotFoundException::class.java) {
            transactionalPaymentOperations.setInProgress(paymentId, paypalId)
        }

        // Verify interactions
        verify { paymentRepository.findByIdOrNull(paymentId) }

    }

    @Test
    fun `setInProgress return because payment status already PAYED`() {
        // Given
        val paymentId=1L
        val paypalId="TOKEN"
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.PAYED,
            paymentAmount = 100.0,
            paypalOrderId = "PROVA"
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByIdOrNull(paymentId) } returns payment


        transactionalPaymentOperations.setInProgress(paymentId, paypalId)

        // Then
        Assertions.assertEquals(payment.reservationId, 123L)
        Assertions.assertEquals(PaymentStatus.PAYED, payment.status)
        Assertions.assertEquals(100.0, payment.paymentAmount)
        Assertions.assertEquals(payment.paypalOrderId, "PROVA")

        // Verify interactions
        verify { paymentRepository.findByIdOrNull(paymentId) }

    }

    @Test
    fun `setInProgress return because payment status already CANCELLED`() {
        // Given
        val paymentId=1L
        val paypalId="TOKEN"
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.CANCELLED,
            paymentAmount = 100.0,
            paypalOrderId = "PROVA"
        )

        // Mock repository to return null (no existing payment)
        every { paymentRepository.findByIdOrNull(paymentId) } returns payment


        transactionalPaymentOperations.setInProgress(paymentId, paypalId)

        // Then
        Assertions.assertEquals(payment.reservationId, 123L)
        Assertions.assertEquals(PaymentStatus.CANCELLED, payment.status)
        Assertions.assertEquals(100.0, payment.paymentAmount)
        Assertions.assertEquals(payment.paypalOrderId, "PROVA")

        // Verify interactions
        verify { paymentRepository.findByIdOrNull(paymentId) }

    }

    /*###############################################################*/

    /*################ setPaid unit test #######################*/
    @Test
    fun `setPaid sets payment status to PAYED`() {
        // Given
        val paypalId = "paypal-123"
        val payment = Payment(
            reservationId = 1L,
            status = PaymentStatus.PENDING,
            paymentAmount = 50.0,
            paypalOrderId = paypalId
        )

        every { paymentRepository.findByPaypalOrderId(paypalId) } returns payment

        // When
        transactionalPaymentOperations.setPaid(paypalId)

        // Then
        Assertions.assertEquals(PaymentStatus.PAYED, payment.status)
        verify { paymentRepository.findByPaypalOrderId(paypalId) }
    }


    /*###############################################################*/

    /*################ setCompleted unit test #######################*/

    @Test
    fun `setCompleted sets payment status to COMPLETED and saves it`() {
        // Given
        val paypalId = "paypal-456"
        val payment = Payment(
            reservationId = 2L,
            status = PaymentStatus.PAYED,
            paymentAmount = 75.0,
            paypalOrderId = paypalId
        )

        every { paymentRepository.findByPaypalOrderId(paypalId) } returns payment
        every { paymentRepository.save(payment) } returns payment

        // When
        transactionalPaymentOperations.setCompleted(paypalId)

        // Then
        Assertions.assertEquals(PaymentStatus.COMPLETED, payment.status)
        verify { paymentRepository.findByPaypalOrderId(paypalId) }
        verify { paymentRepository.save(payment) }
    }

    /*###############################################################*/

    /*################ setCancelled unit test #######################*/

    @Test
    fun `setCancelled sets payment status to CANCELLED`() {
        // Given
        val paypalId = "paypal-789"
        val payment = Payment(
            reservationId = 3L,
            status = PaymentStatus.PENDING,
            paymentAmount = 30.0,
            paypalOrderId = paypalId
        )

        every { paymentRepository.findByPaypalOrderId(paypalId) } returns payment

        // When
        transactionalPaymentOperations.setCancelled(paypalId)

        // Then
        Assertions.assertEquals(PaymentStatus.CANCELLED, payment.status)
        verify { paymentRepository.findByPaypalOrderId(paypalId) }
    }

    /*###############################################################*/

    /*################ createPayPalCaptureEvent unit test #######################*/

    @Test
    fun `createPayPalCaptureEvent saves and deletes outbox event`() {
        // Given
        val paypalToken = "paypal-token-abc"
        val event = PaypalOutboxEvent(paypalToken = paypalToken)

        every { outboxEventRepository.save(any()) } returns event
        every { outboxEventRepository.delete(event) } just runs

        // When
        transactionalPaymentOperations.createPayPalCaptureEvent(paypalToken)

        // Then
        verify { outboxEventRepository.save(match { it.paypalToken == paypalToken }) }
        verify { outboxEventRepository.delete(event) }
    }

    /*###############################################################*/

    /*################ getPaymentStatus unit test #######################*/
    @Test
    fun `getPaymentStatus returns correct status`() {
        // Given
        val paypalToken = "paypal-xyz"
        val payment = Payment(
            reservationId = 10L,
            status = PaymentStatus.PAYED,
            paymentAmount = 80.0,
            paypalOrderId = paypalToken
        )

        every { paymentRepository.findByPaypalOrderId(paypalToken) } returns payment

        // When
        val status = transactionalPaymentOperations.getPaymentStatus(paypalToken)

        // Then
        Assertions.assertEquals(PaymentStatus.PAYED, status)
        verify { paymentRepository.findByPaypalOrderId(paypalToken) }
    }

    /*###############################################################*/

    /*################ getPayment unit test #######################*/
    @Test
    fun `getPayment returns the payment with matching PayPal token`() {
        // Given
        val paypalToken = "paypal-token-123"
        val payment = Payment(
            reservationId = 99L,
            status = PaymentStatus.PENDING,
            paymentAmount = 45.0,
            paypalOrderId = paypalToken
        )

        every { paymentRepository.findByPaypalOrderId(paypalToken) } returns payment

        // When
        val result = transactionalPaymentOperations.getPayment(paypalToken)

        // Then
        Assertions.assertEquals(payment, result)
        verify { paymentRepository.findByPaypalOrderId(paypalToken) }
    }

}