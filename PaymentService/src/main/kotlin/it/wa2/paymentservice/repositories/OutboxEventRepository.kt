package it.wa2.paymentservice.repositories

import it.wa2.paymentservice.entities.PaypalOutboxEvent
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository: JpaRepository<PaypalOutboxEvent, Long> {
}