package it.wa2.reservationservice.dtos

data class PaymentOrderRequestDTO(
    val reservationId: Long,
    val paymentAmount: Double,
)
