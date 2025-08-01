package it.wa2.reservationservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L
    var category: String = ""
}