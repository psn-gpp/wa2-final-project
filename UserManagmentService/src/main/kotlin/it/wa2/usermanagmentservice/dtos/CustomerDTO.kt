package it.wa2.usermanagmentservice.dtos

import it.wa2.usermanagmentservice.entities.Customer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

data class CustomerDTO(

    val id: Long,

    @field:Valid
    val genericUserData: GenericUserDTO,

    @field:NotNull(message = "date of birth required")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @field:Past
    val dateOfBirth: Date,

    val reliabilityScores: Int = 5,

    @field:NotBlank
    val drivingLicence: String,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @field:NotNull
    //@field:Past
    val expirationDate: Date,
) {
    fun toEntity(): Customer = Customer(
        genericUserData = this.genericUserData.toEntity(),
        dateOfBirth = this.dateOfBirth,
        reliabilityScores = this.reliabilityScores,
        drivingLicense = this.drivingLicence,
        expirationDate = this.expirationDate,
    )
}






