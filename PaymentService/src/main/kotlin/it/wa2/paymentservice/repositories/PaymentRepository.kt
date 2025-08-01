package it.wa2.paymentservice.repositories

import it.wa2.paymentservice.entities.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long>{
    fun findByPaypalOrderId(paypalOrderId: String): Payment
    fun findByReservationId(reservationId: Long) : Payment?
}