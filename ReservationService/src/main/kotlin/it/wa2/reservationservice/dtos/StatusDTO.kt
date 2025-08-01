package it.wa2.reservationservice.dtos

import it.wa2.reservationservice.entities.Status
import jakarta.validation.constraints.Pattern

data class StatusDTO (
    var id : Long,

    @field:Pattern(
        regexp = "^(PENDING|APPROVED|REJECTED|PAYED|PAYMENT_REFUSED|ON_COURSE|TERMINATED)$",
        message = "The status should have value: PENDING|APPROVED|REJECTED|PAYED|PAYMENT_REFUSED|ON_COURSE|TERMINATED"
    )
    var status : String,
){
    fun toEntity () = Status(
        reservation = mutableListOf(),
        status = status
    )
}