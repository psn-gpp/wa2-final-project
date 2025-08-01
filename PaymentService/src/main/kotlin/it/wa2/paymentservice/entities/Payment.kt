package it.wa2.paymentservice.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Payment(

    @Column(nullable = false)
    var reservationId: Long,

    @Enumerated(EnumType.STRING)
    var status: PaymentStatus,   // dal diagramma a blocchi del lab

    @Column(nullable = false)
    var paymentAmount: Double,

    // Informazioni PayPal
    @Column(unique = true)
    var paypalOrderId: String? = null,

    var paypalPaymentId: String? = null,

    // Timestamp utili per il tracking
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var paymentDate: LocalDateTime? = null,

    @Version  // Per gestire la concorrenza
    var version: Long = 0

    ) : BaseEntity()

enum class PaymentStatus {
    CREATED,
    PENDING,
    CANCELLED,
    PAYED,
    COMPLETED,
}