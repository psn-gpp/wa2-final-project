package it.wa2.usermanagmentservice.entities

import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne

/**
 * User rappresenta l'utente generico: ogni utente poi avrà un ruolo preciso (con informazioni aggiuntive)
 *
 * Le sotto classi di User avranno lo stesso id di User -> perchè ogni riferimento a User usa @MapsId
 *
 * User estende [BaseEntity] che ha un id ed estende equal() e hash()
 */


@Entity
class GenericUser (

    @OneToOne(mappedBy = "genericUserData", cascade = [CascadeType.ALL])
    var customer: Customer? = null,
    @OneToOne(mappedBy = "genericUserData", cascade = [CascadeType.ALL])
    var employee: Employee? = null,

    var keycloakId: String = "",

    var name: String = "",
    var surname: String = "",
    @Column(unique = true)
    var email: String = "",
    var phone: String = "",
    var address: String = "",
    var city: String = ""

) : BaseEntity() {

    fun toDTO() = GenericUserDTO(
        id = this.id,
        name = this.name,
        surname = this.surname,
        email = this.email,
        phone = this.phone,
        address = this.address,
        city = this.city,
        keycloakId = this.keycloakId
    )
    fun update(newUser: GenericUser) {
        name = newUser.name
        surname = newUser.surname
        email = newUser.email
        phone = newUser.phone
        address = newUser.address
        city = newUser.city
    }
}


