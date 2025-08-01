package it.wa2.usermanagmentservice.entities

import it.wa2.usermanagmentservice.dtos.EmployeeDTO
import jakarta.persistence.*


@Entity
class Employee (
    @OneToOne(cascade = [CascadeType.ALL])
    @MapsId
    var genericUserData: GenericUser,

    @ManyToOne
    var role: Role,  // Staff, Fleet Manager, Manager
    var salary: Double

) : BaseEntity() {

    fun toDTO(): EmployeeDTO = EmployeeDTO(
        id = this.id,
        genericUserData = this.genericUserData.toDTO(),
        role = this.role.toDTO(),
        salary = this.salary,
    )

    fun update(newEmployee: Employee) {
        this.genericUserData.update(newEmployee.genericUserData)
        this.role = newEmployee.role
        this.salary = newEmployee.salary
    }
}


