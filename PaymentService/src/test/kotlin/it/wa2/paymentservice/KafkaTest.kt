package it.wa2.paymentservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.repositories.PaymentRepository
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.web.servlet.MockMvc
import java.util.concurrent.TimeUnit
import io.mockk.*
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.entities.PaypalOutboxEvent
import it.wa2.paymentservice.services.PaymentService
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*


@SpringBootTest
@AutoConfigureMockMvc
class KafkaTest : IntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockkBean
    lateinit var paymentService: PaymentService


    /***
     * Il test simula l’arrivo di un evento di pagamento su Kafka e verifica che il sistema aggiorni
     * lo stato del pagamento a COMPLETED, senza chiamare servizi esterni reali.
     */
    @Test
    fun `PAYED to COMPLETED when event received`() {
        // Questo simula la situazione reale in cui un pagamento è stato effettuato e si attende la conferma/capture da PayPal.
        // creo un pagamento in stato PAYED
        val payment = Payment(
            reservationId = 123L,
            status = PaymentStatus.PAYED,
            paymentAmount = 50.0,
            paypalOrderId = "test-paypal-token",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        // Mock: non faccio una chiamata reale a paypal (quindi faccio mock di paymentService)
        // il service deve restituire un ordine con status COMPLETED
        val fakeOrder = com.paypal.sdk.models.Order().apply {
            status = com.paypal.sdk.models.OrderStatus.COMPLETED
        }
        every { paymentService.captureOrder("test-paypal-token") } returns fakeOrder

        // Crea l'evento
        val event = PaypalOutboxEvent(paypalToken = "test-paypal-token")
        val message = objectMapper.writeValueAsString(event)

        // Act: invia il messaggio su Kafka
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        // Wait: aspetta che il listener processi il messaggio e aggiorni lo stato
        await().atMost(10, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("test-paypal-token").status == PaymentStatus.COMPLETED
        }
    }

    @Test
    fun `already COMPLETED payment is ignored`() {
        val payment = Payment(
            reservationId = 2L,
            status = PaymentStatus.COMPLETED,
            paymentAmount = 20.0,
            paypalOrderId = "token-completed",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        val event = PaypalOutboxEvent(paypalToken = "token-completed")
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        // Attendi e verifica che lo stato resti COMPLETED
        await().atMost(5, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("token-completed").status == PaymentStatus.COMPLETED
        }
    }

    @Test
    fun `PENDING payment is cancelled`() {
        // Arrange
        val payment = Payment(
            reservationId = 456L,
            status = PaymentStatus.PENDING,
            paymentAmount = 20.0,
            paypalOrderId = "pending-token",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        val event = PaypalOutboxEvent(paypalToken = "pending-token")
        val message = objectMapper.writeValueAsString(event)

        // Act
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        // Assert
        await().atMost(10, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("pending-token").status == PaymentStatus.CANCELLED
        }
    }

    @Test
    fun `CANCELLED payment is ignored`() {
        val payment = Payment(
            reservationId = 4L,
            status = PaymentStatus.CANCELLED,
            paymentAmount = 40.0,
            paypalOrderId = "token-cancelled",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        val event = PaypalOutboxEvent(paypalToken = "token-cancelled")
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        await().atMost(5, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("token-cancelled").status == PaymentStatus.CANCELLED
        }
    }

    @Test
    fun `CREATED payment is ignored`() {
        val payment = Payment(
            reservationId = 5L,
            status = PaymentStatus.CREATED,
            paymentAmount = 50.0,
            paypalOrderId = "token-created",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        val event = PaypalOutboxEvent(paypalToken = "token-created")
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        await().atMost(5, TimeUnit.SECONDS).until {
            paymentRepository.findByPaypalOrderId("token-created").status == PaymentStatus.CREATED
        }
    }



    // 7. Errore durante la capture (es. eccezione PayPal) → messaggio non ack, stato resta PAYED
    @Test
    fun `PAYED payment, error during capture, state remains PAYED`() {
        val payment = Payment(
            reservationId = 6L,
            status = PaymentStatus.PAYED,
            paymentAmount = 60.0,
            paypalOrderId = "token-error",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        every { paymentService.captureOrder("token-error") } throws RuntimeException("PayPal error")

        val event = PaypalOutboxEvent(paypalToken = "token-error")
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        // Attendi un po', lo stato deve restare PAYED (il messaggio verrà riprovato da Kafka)
        Thread.sleep(3000)
        assertEquals(PaymentStatus.PAYED, paymentRepository.findByPaypalOrderId("token-error").status)
    }


    // 8. PAYED ma PayPal non restituisce COMPLETED → stato non aggiornato
    @Test
    fun `PAYED payment, PayPal does not return COMPLETED, state remains PAYED`() {
        val payment = Payment(
            reservationId = 7L,
            status = PaymentStatus.PAYED,
            paymentAmount = 70.0,
            paypalOrderId = "token-not-completed",
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAndFlush(payment)

        val fakeOrder = com.paypal.sdk.models.Order().apply {
            status = com.paypal.sdk.models.OrderStatus._UNKNOWN // o un altro stato diverso da COMPLETED
        }
        every { paymentService.captureOrder("token-not-completed") } returns fakeOrder

        val event = PaypalOutboxEvent(paypalToken = "token-not-completed")
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("paypal.public.paypal_outbox_events", message)

        // Attendi un po', lo stato deve restare PAYED
        Thread.sleep(3000)
        assertEquals(PaymentStatus.PAYED, paymentRepository.findByPaypalOrderId("token-not-completed").status)
    }
}

