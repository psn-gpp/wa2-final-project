package it.wa2.reservationservice.services

import feign.Body
import it.wa2.reservationservice.dtos.PaymentOrderRequestDTO
import it.wa2.reservationservice.dtos.PaymentOrderResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.net.URI

@FeignClient(name = "payment-service", url = "\${payment.service.url}")
interface PaymentClient {
    @GetMapping("/api/v1/orders/order/{token}")
    fun getPaypalOrder(@PathVariable token: String): PaymentOrderResponseDTO
    @PostMapping("/api/v1/orders/create")
    fun createPayPalOrder(@RequestBody paymentOrderRequestDTO: PaymentOrderRequestDTO) : String
}