package it.wa2.paymentservice.entities

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

    @Column(nullable = false)
    @JsonProperty("paypal_token")
    val paypalToken: String,

) : BaseEntity()


