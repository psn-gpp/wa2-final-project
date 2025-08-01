package it.wa2.paymentservice.entities

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.util.ProxyUtils

@MappedSuperclass
open class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
) {
    override fun hashCode(): Int {
        return if (id != 0L) {
            id.hashCode()
        } else {
            super.hashCode()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (this === other) return true
        if ((javaClass != BaseEntity::class.java && other != BaseEntity::class.java) && javaClass != ProxyUtils.getUserClass(other)) return false
        if (other !is BaseEntity) return false
        return if (0L == id) false
        else this.id == other.id
    }
}