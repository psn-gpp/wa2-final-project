package it.wa2.usermanagmentservice.entities

import it.wa2.usermanagmentservice.dtos.RoleDTO
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Role(

    @OneToMany(mappedBy = "role", cascade = [CascadeType.ALL])
    var employee: MutableList<Employee>,
    @Column(updatable = false, unique = true)
    var nameRole: String

) : BaseEntity() {

    fun toDTO(): RoleDTO = RoleDTO(
        id = this.id,
        nameRole = this.nameRole,
    )

}
