package it.wa2.reservationservice.dtos

enum class PaymentStatus {
    CREATED,
    PENDING,
    CANCELLED,
    PAYED,
    COMPLETED,
}

data class PaymentOrderResponseDTO (
    val reservationId: Long = 0,
    val paypalOrderId: String? = null,
    val status: PaymentStatus? = null,
    val message: String? = null,
    val error: String? = null
)