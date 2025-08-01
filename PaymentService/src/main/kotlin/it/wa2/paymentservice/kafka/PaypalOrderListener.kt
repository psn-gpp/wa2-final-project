package it.wa2.paymentservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.paypal.sdk.models.OrderStatus
import it.wa2.paymentservice.controllers.PaymentController
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.entities.PaypalOutboxEvent
import it.wa2.paymentservice.services.PaymentService
import it.wa2.paymentservice.services.TransactionalPaymentOperations
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


import java.time.Duration

@Component
class PaypalOrderListener(
    private val transactionalPaymentOperations: TransactionalPaymentOperations,
    private val paymentService: PaymentService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(PaypalOrderListener::class.java)

    /**
     * E' un [KafkaListener], estrapola l'evento [PaypalOutboxEvent] da [message]
     * mediante [paypalToken] contenuto in [PaypalOutboxEvent] estrapola lo stato del [Payment] -->[TransactionalPaymentOperations.getPaymentStatus]
     * aggiorna lo stato del Payment nel db
     */
    @KafkaListener(topics = ["paypal.public.paypal_outbox_events"], groupId = "\${spring.kafka.consumer.group-id:paypal-order-listener}")
    @Transactional
    fun processPaypalOrder(message: String,  @Header(KafkaHeaders.ACKNOWLEDGMENT) ack: Acknowledgment) {
        logger.info("Received PayPal order message from Kafka: {}", message)
        try {
            if (message.isEmpty()) {
                logger.warn("Received empty message from Kafka, acknowledging and skipping.")
                ack.acknowledge()
                return;
            }
            // Parse the message from Kafka using the schema
            val eventData = objectMapper.readValue(message, PaypalOutboxEvent::class.java)

            val paypalToken = eventData.paypalToken

            logger.debug("Parsed PaypalOutboxEvent: paypalToken={}", paypalToken)

            //val payerId = eventData.payerId

            val paymentStatus = transactionalPaymentOperations.getPaymentStatus(paypalToken)
            logger.info("Current payment status for token {}: {}", paypalToken, paymentStatus)

            when (paymentStatus) {
                PaymentStatus.COMPLETED -> {
                    logger.info("Payment with token {} is already COMPLETED. Acknowledging message.", paypalToken)
                    //already processed, ack the message
                    ack.acknowledge()
                }

                PaymentStatus.PAYED -> {
                    logger.info("Payment with token {} is PAYED. Attempting to capture order.", paypalToken)
                    /**
                     * esegue la capture del pagamento
                     */
                    val order = paymentService.captureOrder(paypalToken /*payerId*/)
                    logger.info("Capture result for token {}: PayPal order status={}", paypalToken, order.status)
                    if (order.status == OrderStatus.COMPLETED) {
                        transactionalPaymentOperations.setCompleted(paypalToken)
                        logger.info("Payment with token {} set to COMPLETED.", paypalToken)
                        ack.acknowledge()
                    }
                }

                PaymentStatus.PENDING -> {
                    logger.info("Payment with token {} is PENDING. Cancelling payment.", paypalToken)
                    transactionalPaymentOperations.setCancelled(paypalToken)
                    logger.info("Payment with token {} set to CANCELLED.", paypalToken)
                    ack.acknowledge()
                }

                else -> {
                    logger.warn("Ignoring PayPal order event with token {} and status {}.", paypalToken, paymentStatus)
                    ack.acknowledge()
                }
            }
        } catch (e: Exception) {
            ack.nack(Duration.ofSeconds(5))
            // Log the error and don't acknowledge the message, so it will be retried
            logger.error("Error processing PayPal order message: {}", e.message, e)
        }
    }
}