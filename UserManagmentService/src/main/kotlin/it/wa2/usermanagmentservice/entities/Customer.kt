package it.wa2.usermanagmentservice.entities

import it.wa2.usermanagmentservice.dtos.CustomerDTO
import jakarta.persistence.*
import java.util.*
import java.util.prefs.Preferences


/**
 * Un Customer fa riferimento ad uno User + altre info
 *
 * L'id di Customer è uguale a quello di userData
 *
 * ## Examples
 *
 * ```kotlin
 * val a: Customer
 * val idA1 = a.userData.id
 * val idA2 = a.id
 *
 * assert(idA1==idA2)
 * ```
 *
 */

@Entity
class Customer (

    @OneToOne(cascade = [CascadeType.ALL])
    @MapsId
    var genericUserData : GenericUser,

    @Temporal(TemporalType.DATE)                        // ?????????
    var dateOfBirth: Date = Date(),
    var reliabilityScores: Int = 0,
    @Column(unique = true)
    var drivingLicense: String = "",
    @Temporal(TemporalType.DATE)
    var expirationDate: Date = Date(),

) : BaseEntity() {
    fun toDTO(): CustomerDTO = CustomerDTO(
        id = this.genericUserData.id,
        genericUserData = this.genericUserData.toDTO(),
        dateOfBirth = this.dateOfBirth,
        reliabilityScores = this.reliabilityScores,
        drivingLicence = this.drivingLicense,
        expirationDate = this.expirationDate,
    )

    fun update(newCustomer: Customer) {
        genericUserData.update(newCustomer.genericUserData)
        dateOfBirth = newCustomer.dateOfBirth
        reliabilityScores = newCustomer.reliabilityScores
        drivingLicense = newCustomer.drivingLicense
        expirationDate = newCustomer.expirationDate
    }
}

