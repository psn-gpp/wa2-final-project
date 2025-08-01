package it.wa2.paymentservice.controllers


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.dtos.PaymentOrderResponseDTO
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.services.PaymentService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:8080"])
@RestController
@RequestMapping("/api/v1/orders")
@Validated
class PaymentController(private val paymentService: PaymentService) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    /**
     * Crea un ordine paypal, richiede come parametro [PaymentOrderRequestDTO]
     */
    @Operation(
        summary = "Create a new request for payment",
        description = "Use to pay",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PostMapping("/create")
    fun createPaymentOrder(
        @Valid @RequestBody paymentOrderRequestDTO: PaymentOrderRequestDTO
    ): String {

        val response = paymentService.createPaymentOrder(paymentOrderRequestDTO)

        logger.info("Payment order created successfully: {}", response)

        return response
    }

    /**
     * Arrivo qui se l'utente ha autorizzato il pagamento
     */
    @GetMapping("/capture")
    fun capturePaymentOrder(
        @RequestParam("token") token: String,
       // @RequestParam("PayerId") payerId: String
        redirect: HttpServletResponse
    ): String {

        val response = paymentService.capturePaymentOrder(token, redirect)

        logger.info("Payment order captured successfully: {}", response)

        return response
    }

    /**
     * Arrivo qui se l'utente ha cancellato il pagamento
     */
    @GetMapping("/cancel")
    fun cancelPaymentOrder(@RequestParam("token") token: String, redirect: HttpServletResponse): String {

        val response = paymentService.cancelPaymentOrder(token, redirect)

        logger.info("Payment order cancelled successfully: {}", response)

        return response

    }


    @GetMapping("/order/{token}")
    fun getPaypalOrder(@PathVariable token: String): PaymentOrderResponseDTO {
        logger.info("Getting payment order with token: {}", token)
        val payment = paymentService.getPaymentOrder(token)
        return payment
    }
}