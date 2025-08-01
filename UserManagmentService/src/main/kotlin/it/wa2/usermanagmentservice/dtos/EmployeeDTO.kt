package it.wa2.usermanagmentservice.dtos

import it.wa2.usermanagmentservice.entities.Employee
import it.wa2.usermanagmentservice.entities.Role
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EmployeeDTO (
    val id: Long,

    @field:Valid
    val genericUserData: GenericUserDTO,

    @field:Valid
    val role: RoleDTO,

    @field:NotNull
    val salary: Double,
) {
    fun toEntity(role: Role): Employee = Employee(
        genericUserData = this.genericUserData.toEntity(),
        role = role,
        salary = this.salary
    )
}