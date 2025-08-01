package it.wa2.paymentservice.services

import com.paypal.sdk.models.Order
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.dtos.PaymentOrderResponseDTO
import jakarta.servlet.http.HttpServletResponse

interface PaymentService {

    fun captureOrder(token: String, /*payerId: String*/): Order
    fun createPaymentOrder(paymentOrderRequestDTO: PaymentOrderRequestDTO): String
    fun capturePaymentOrder(token: String, response: HttpServletResponse): String
    fun cancelPaymentOrder(token: String, response: HttpServletResponse): String
    fun getPaymentOrder(token: String): PaymentOrderResponseDTO
}