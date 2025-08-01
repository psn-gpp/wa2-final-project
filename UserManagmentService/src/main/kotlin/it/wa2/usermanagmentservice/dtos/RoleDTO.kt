package it.wa2.usermanagmentservice.dtos

import it.wa2.usermanagmentservice.entities.Role
import jakarta.validation.constraints.Pattern

data class RoleDTO (
    var id: Long,
    @field:Pattern(
        regexp = "^(Staff|Manager|Fleet Manager)$",
        message = "Il ruolo deve essere uno tra: Staff, Manager, Fleet manager"
    )
    var nameRole: String,
) {
    fun toEntity() = Role(
        employee = mutableListOf(),
        nameRole = nameRole,
    )
}