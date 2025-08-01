package it.wa2.usermanagmentservice.repositories


import it.wa2.usermanagmentservice.entities.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    fun findFirstByNameRole(nameRole: String): Role?
}