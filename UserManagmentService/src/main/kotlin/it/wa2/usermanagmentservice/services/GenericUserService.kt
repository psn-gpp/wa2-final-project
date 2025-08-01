package it.wa2.usermanagmentservice.services


import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface GenericUserService {
    fun getUsers(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
    ): Page<GenericUserDTO>
    fun getUserById(userId: Long): GenericUserDTO
    fun updateUserById(userId: Long, @Valid genericUserDTO: GenericUserDTO): GenericUserDTO
    fun getUserByKeycloakId(keycloakId: String): GenericUserDTO
}

