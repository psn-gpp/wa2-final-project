package it.wa2.reservationservice.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.persistence.*
import java.time.LocalDateTime

//TODO creare migration

@Entity
@Table(name = "paypal_outbox_events")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class PaypalOutboxEvent(
    //val reservationId: Long,
    //var paymentStatus: String,
    //val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    @JsonProperty("paypal_token")
    val paypalToken: String,

    /*
    @Column(nullable = false)
    @JsonProperty("payer_id")
    val payerId: String,

     */

) : BaseEntity()

//TODO spostare nel paymentServiceImplementations --> producer --> dove riceviamo la risposta di conferma pagamento (parte critica)
/*val event = PaypalOutboxEvent(
    reservationId = payment.reservationId,
    paymentStatus = payment.status
)
outboxRepository.save(event)
*/

