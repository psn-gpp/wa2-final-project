package it.wa2.paymentservice.services

import it.wa2.paymentservice.advices.PaymentNotFoundException
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.entities.PaypalOutboxEvent
import it.wa2.paymentservice.repositories.OutboxEventRepository
import it.wa2.paymentservice.repositories.PaymentRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TransactionalPaymentOperations(
    private val paymentRepository: PaymentRepository,
    private val outboxEventRepository: OutboxEventRepository,
) {
    private val logger = LoggerFactory.getLogger(TransactionalPaymentOperations::class.java)

    /**
     * aggiunge [Payment] ad db se non esiste già una entry con lo stesso reservationId e stato [PaymentStatus.COMPLETED]
     */
    @Transactional
    fun addPayment(payment: Payment): Payment? {

        val old = paymentRepository.findByReservationId(payment.reservationId)

        if (old != null && old.status == PaymentStatus.COMPLETED ) {
            logger.warn("Attempted to add duplicate payment for reservationId={}, already COMPLETED", payment.reservationId)
            return null
        }

        payment.status = PaymentStatus.CREATED

        // TODO: i prof non salvano -> sistemare prima kafka e poi vedere se funziona anche senza salvare (ne dubito)

        val saved =  paymentRepository.save(payment)
        logger.info("Payment created: id={}, reservationId={}, amount={}", saved.id, saved.reservationId, saved.paymentAmount)
        return saved
    }
    @Transactional
    fun setInProgress(paymentId: Long, paypalId: String) {
        val payment = paymentRepository.findByIdOrNull(paymentId) ?: run {
            logger.error("Payment not found for id={}", paymentId)
            throw PaymentNotFoundException("Pagamento $paymentId non trovato")
        }

        if(payment.status == PaymentStatus.PAYED || payment.status == PaymentStatus.CANCELLED){
            logger.warn("Cannot set payment in progress: id={} is already in status={}", paymentId, payment.status)
            return
        }

        payment.paypalOrderId = paypalId
        payment.status = PaymentStatus.PENDING
        logger.info("Payment set to PENDING: id={}, paypalOrderId={}", paymentId, paypalId)
    }

    @Transactional
    fun setPaid( paypalId: String) {
        val payment = paymentRepository.findByPaypalOrderId(paypalId)
        payment.status = PaymentStatus.PAYED
        logger.info("Payment set to PAYED: id={}, paypalOrderId={}", payment.id, paypalId)
    }

    @Transactional
    fun setCompleted( paypalId: String) {
        val payment = paymentRepository.findByPaypalOrderId(paypalId)
        payment.status = PaymentStatus.COMPLETED
        paymentRepository.save(payment)
        logger.info("Payment set to COMPLETED: id={}, paypalOrderId={}", payment.id, paypalId)
    }

    @Transactional
    fun setCancelled( paypalId: String) {
        val payment = paymentRepository.findByPaypalOrderId(paypalId)
        payment.status = PaymentStatus.CANCELLED
        logger.info("Payment set to CANCELLED: id={}, paypalOrderId={}", payment.id, paypalId)
    }

    /**
     * crea un [PaypalOutboxEvent]
     */
    @Transactional
    fun createPayPalCaptureEvent(paypalToken: String, /*payerId: String*/) {
        val event = PaypalOutboxEvent(
            paypalToken = paypalToken,
           // payerId = payerId,
        )
        val cdcOptimization = outboxEventRepository.save(event)
        outboxEventRepository.delete(cdcOptimization)
        logger.info("PayPal outbox event created and deleted for token={}", paypalToken)
    }

    fun getPaymentStatus(paypalToken: String): PaymentStatus {
        val payment = paymentRepository.findByPaypalOrderId(paypalToken)
        logger.debug("Fetched payment status: paypalOrderId={}, status={}", paypalToken, payment.status)
        return payment.status
    }

    fun getPayment(paypalToken: String): Payment {
        val payment = paymentRepository.findByPaypalOrderId(paypalToken)
        logger.debug("Fetched payment: paypalOrderId={}, id={}", paypalToken, payment.id)
        return payment
    }
}