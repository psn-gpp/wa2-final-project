package it.wa2.usermanagmentservice.dtos



import it.wa2.usermanagmentservice.entities.GenericUser
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern


data class GenericUserDTO(
    val id: Long,

    @field:NotNull
    val keycloakId: String = "",

    @field:NotBlank
    val name: String = "",

    @field:NotBlank
    val surname: String = "",

    @field:Email
    val email: String = "",


    val phone: String = "",


    val address: String = "",


    val city: String = "",

)

fun GenericUserDTO.toEntity() = GenericUser(
    name = this.name,
    surname = this.surname,
    email = this.email,
    phone = this.phone,
    address = this.address,
    city = this.city,
    keycloakId = this.keycloakId,
)