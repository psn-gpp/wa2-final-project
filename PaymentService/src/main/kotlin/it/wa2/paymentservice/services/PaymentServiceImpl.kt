package it.wa2.paymentservice.services

import com.paypal.sdk.PaypalServerSdkClient
import com.paypal.sdk.models.*
import it.wa2.paymentservice.advices.PaymentCreationBadRequest
import it.wa2.paymentservice.advices.PaymentCreationInternalServerError
import it.wa2.paymentservice.advices.PaymentException
import it.wa2.paymentservice.controllers.PaymentController
import it.wa2.paymentservice.dtos.PaymentOrderRequestDTO
import it.wa2.paymentservice.dtos.PaymentOrderResponseDTO
import it.wa2.paymentservice.entities.PaypalOutboxEvent
import it.wa2.paymentservice.entities.Payment
import it.wa2.paymentservice.entities.PaymentStatus
import it.wa2.paymentservice.repositories.OutboxEventRepository
import it.wa2.paymentservice.repositories.PaymentRepository
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.*


@Service
@Primary
@Validated
@Transactional
class PaymentServiceImpl(
    private val payPalClient: PaypalServerSdkClient,
    private val transactionalPaymentOperations: TransactionalPaymentOperations,
    @Value("\${paypal.currency}") private val currency: String,
    @Value("\${paypal.return-url}") private val returnUrl: String,
    @Value("\${paypal.cancel-url}") private val cancelUrl: String,
) : PaymentService {

    private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    /**
     * Creates a PayPal order for Payment checkout
     */
    fun createPaypalOrder(
        paymentInfo: Payment,
    ): Order? {
        logger.info("Creating PayPal order for reservationId={}, amount={}", paymentInfo.reservationId, paymentInfo.paymentAmount)
        val orderRequest = OrderRequest().apply {
            intent = CheckoutPaymentIntent.CAPTURE
        }


        val item = listOf(
            Item().apply {
                name = paymentInfo.reservationId.toString()
                unitAmount = Money().apply {
                    currencyCode = currency
                    value = String.format(Locale.US, "%.2f", paymentInfo.paymentAmount)
                }
                quantity = "1"

            })


        val amount = AmountWithBreakdown().apply {
            currencyCode = currency
            value = String.format(Locale.US, "%.2f", paymentInfo.paymentAmount)
            breakdown = AmountBreakdown().apply {
                itemTotal = Money().apply {
                    currencyCode = currency
                    value = String.format(Locale.US, "%.2f", paymentInfo.paymentAmount)
                }
            }
        }


        val purchaseUnitRequest = PurchaseUnitRequest().apply {
            this.amount = amount
            this.items = item
        }



        orderRequest.purchaseUnits = listOf(purchaseUnitRequest)

        val applicationContext = OrderApplicationContext()
        applicationContext.returnUrl = returnUrl
        applicationContext.cancelUrl = cancelUrl

        orderRequest.applicationContext = applicationContext

        val createOrderInput = CreateOrderInput();
        createOrderInput.body = orderRequest;

        try {
            val response = payPalClient.ordersController.createOrder(createOrderInput)
            val order = response.result
            logger.info("PayPal order created successfully: paypalOrderId={}", order?.id)
            return order
        } catch (e: Exception) {
            logger.error("Error creating PayPal order for reservationId={}: {}", paymentInfo.reservationId, e.message, e)
            return null;
        }
    }

    /**
     * Captures payment for an approved order
     */
    override fun captureOrder(token: String, /*payerId: String*/): Order {
        logger.info("Capturing PayPal order with token={}", token)

        val paymentSource = OrderCaptureRequestPaymentSource()
        paymentSource.token = Token(token, TokenType.BILLING_AGREEMENT);

        val captureOrderRequest = OrderCaptureRequest()
        captureOrderRequest.paymentSource = paymentSource


        val captureOrderInput = CaptureOrderInput();
        captureOrderInput.body = captureOrderRequest;
        captureOrderInput.id = token

        val result = payPalClient.ordersController.captureOrder(captureOrderInput)
        logger.info("PayPal order captured: token={}, status={}", token, result.result.status)
        return result.result
    }


    /**
     * Crea un oggetto [Payment] con stato [PaymentStatus.CREATED]
     *
     * prima di salvarlo nel db chiama la [TransactionalPaymentOperations.addPayment]
     *
     * quindi chiama la [createPaypalOrder] --> interagisce con [PaypalServerSdkClient]
     *
     * se [PaypalServerSdkClient] restituisce un Order, chiama la
     * [TransactionalPaymentOperations.setInProgress] che setta lo stato del pagamento a [PaymentStatus.PENDING]
     *
     *
     * restituisce il link per la pagina di paypal
     */
    override fun createPaymentOrder(paymentOrderRequestDTO: PaymentOrderRequestDTO): String {
        logger.info("Received payment order request: reservationId={}, amount={}", paymentOrderRequestDTO.reservationId, paymentOrderRequestDTO.paymentAmount)

        if(paymentOrderRequestDTO.paymentAmount < 0.01){
            logger.warn("Invalid payment amount: {}", paymentOrderRequestDTO.paymentAmount)
            throw PaymentCreationBadRequest("invalid amount")
        }


        // Creo un nuovo Payment
        val payment = Payment(
            reservationId = paymentOrderRequestDTO.reservationId,
            status = PaymentStatus.CREATED,
            paymentAmount = paymentOrderRequestDTO.paymentAmount,
        )

        val newPayment = transactionalPaymentOperations.addPayment(payment) ?: run {
            logger.warn("Duplicate payment for reservationId={}", payment.reservationId)
            throw PaymentCreationBadRequest("payment duplicated")
        }


        /*
        if(newPayment == null) {
            //return "" // TODO: sistemare
            throw PaymentCreationException("payment duplicated")
        }
        */

        // creo l'ordine paypal
        val paypalOrder = createPaypalOrder(payment) ?: run {
            logger.error("Failed to create PayPal order for reservationId={}", payment.reservationId)
            throw PaymentCreationInternalServerError("Non è stato possobile creare l'ordine di pagamento di Paypal")
        }

        /*
        if(paypalOrder == null) {
            return "" // TODO: sistemare
        }*/

        transactionalPaymentOperations.setInProgress(payment.id, paypalOrder.id)
        logger.info("Payment set to IN_PROGRESS: paymentId={}, paypalOrderId={}", payment.id, paypalOrder.id)

        return paypalOrder.links[1].href  ?: run {
            logger.error("PayPal order link is null for reservationId={}", payment.reservationId)
            throw PaymentCreationInternalServerError("Non è stato possobile creare l'ordine di pagamento di Paypal, link null")
        }

    }


    /**
     * setta lo stato del [Payment] a [PaymentStatus.PAYED]
     * esegue la [TransactionalPaymentOperations.createPayPalCaptureEvent]
     * fa la redirect al frontend
     */
    override fun capturePaymentOrder(token: String , response: HttpServletResponse): String {

        logger.info("Setting payment as PAID for token={}", token)
        transactionalPaymentOperations.setPaid(token)

        transactionalPaymentOperations.createPayPalCaptureEvent(token /*payerId*/)
        logger.info("Redirecting to order details for token={}", token)


        val redirectUrl = "http://localhost:8084/ui/loading"

        response.sendRedirect(redirectUrl)
        return redirectUrl


    }

    /**
     * esegue la [TransactionalPaymentOperations.createPayPalCaptureEvent]
     * fa la redirect al frontend
     */
    override fun cancelPaymentOrder(token: String, response: HttpServletResponse): String {
        logger.info("Cancelling payment for token={}", token)
        transactionalPaymentOperations.createPayPalCaptureEvent(token /*payerId*/)

        logger.info("Redirecting to order details for token={}", token)

        val redirectUrl = "http://localhost:8084/ui/loading?cancel=true"
        response.sendRedirect(redirectUrl)
        return redirectUrl
    }

    override fun getPaymentOrder(token: String): PaymentOrderResponseDTO {
        val payment = transactionalPaymentOperations.getPayment(token)
        logger.info("Fetching payment order for token={}", token)
        return PaymentOrderResponseDTO(
            reservationId = payment.reservationId,
            paypalOrderId = token,
            status = payment.status,
            message = "Payment found for token $token",
            error = null
        )
    }

}
