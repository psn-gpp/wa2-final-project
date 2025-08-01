package it.wa2.paymentservice.dtos

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class PaymentOrderRequestDTO(

    @field:NotNull(message = "Reservation id is required")
    @field:Min(value = 0, message = "Reservation id must be equal or greater than 0")
    val reservationId: Long,

    @field:NotNull(message = "Payment amount is required")
    @field:DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    val paymentAmount: Double,
)
