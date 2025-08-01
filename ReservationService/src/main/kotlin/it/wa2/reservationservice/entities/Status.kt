package it.wa2.reservationservice.entities

import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.dtos.StatusDTO
import jakarta.persistence.*
import java.util.*

@Entity
class Status(
    @OneToMany(mappedBy = "status", cascade = [CascadeType.ALL])
    var reservation: MutableList<Reservation>,

    var status: String
) : BaseEntity() {
    fun toDTO() = StatusDTO(
        id = this.id,
        status = this.status
    )

}