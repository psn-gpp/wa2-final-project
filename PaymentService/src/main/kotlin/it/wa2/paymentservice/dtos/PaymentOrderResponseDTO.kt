package it.wa2.paymentservice.dtos

import it.wa2.paymentservice.entities.PaymentStatus

data class PaymentOrderResponseDTO (
    val reservationId: Long = 0,
    val paypalOrderId: String? = null,
    val status: PaymentStatus? = null,
    val message: String? = null,
    val error: String? = null
)