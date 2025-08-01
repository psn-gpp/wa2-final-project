package it.wa2.usermanagmentservice.services


import it.wa2.usermanagmentservice.advices.GenericUserDuplicate
import it.wa2.usermanagmentservice.advices.GenericUserInconsistency
import it.wa2.usermanagmentservice.advices.GenericUserNotFound
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.dtos.toEntity
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Primary
@Validated
class UserServiceImpl(private val genericUserRepository: GenericUserRepository) : GenericUserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    /**
     * Verify if already exsits some user with same email or phone number
     */
    private fun checkDuplicate(genericUserDTO: GenericUserDTO) {
        logger.debug("Checking for duplicates: ${genericUserDTO.email}")
        val currentUser = genericUserRepository.findByIdOrNull(genericUserDTO.id)

        if(currentUser?.email != genericUserDTO.email && genericUserRepository.existsByEmail(genericUserDTO.email)) {
            logger.warn("Duplicate email found: ${genericUserDTO.email}")
            throw GenericUserDuplicate("An user with email ${genericUserDTO.email} already exists.")
        }
        if(currentUser?.phone != genericUserDTO.phone && genericUserRepository.existsByPhone(genericUserDTO.phone)) {
            logger.warn("Duplicate phone found: ${genericUserDTO.phone}")
            throw GenericUserDuplicate("An user with phone ${genericUserDTO.phone} already exists.")
        }

    }

    /**
     * Get all users
     */
    override fun getUsers(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
    ): Page<GenericUserDTO> {
        logger.debug("Fetching users with filters")
        return genericUserRepository.findWithFilters(
            pageable,
            name,
            surname,
            address,
            city,
        ).map { it.toDTO() }
    }

    /**
     * Return the specified user, if not present throw [GenericUserNotFound] exception
     */
    override fun getUserById(userId: Long): GenericUserDTO {
        logger.debug("Fetching user with id: $userId")
        return genericUserRepository.findByIdOrNull(userId)?.toDTO() ?: run {
            logger.warn("User not found with id: $userId")
            throw GenericUserNotFound("User with $userId not found")
        }
    }

    /**
     * Update a user if present, otherwise throw [GenericUserNotFound] exception
     */
    override fun updateUserById(userId: Long, genericUserDTO: GenericUserDTO): GenericUserDTO {
        logger.info("Updating user with id: $userId")
        if(userId != genericUserDTO.id) {
            logger.error("id mismatch: path id=$userId, body id=${genericUserDTO.id}")
            throw GenericUserInconsistency("User with $userId not found")
        }
        checkDuplicate(genericUserDTO)

        val genericUser = genericUserRepository.findByIdOrNull(userId) ?: run {
            logger.warn("User not found with id: $userId")
            throw GenericUserNotFound("User with $userId not found")
        }
        genericUser.update(genericUserDTO.toEntity())
        val updatedUser = genericUserRepository.save(genericUser).toDTO()
        logger.info("User $userId updated successfully")
        return updatedUser
    }

    /**
     * Return the specified user, if not present throw [GenericUserNotFound] exception
     */
    override fun getUserByKeycloakId(keycloakId: String): GenericUserDTO {
        logger.debug("Fetching user with keycloakid: $keycloakId")
        return genericUserRepository.findByKeycloakId(keycloakId)?.toDTO() ?: run {
            logger.warn("User not found with id: $keycloakId")
            throw GenericUserNotFound("User with $keycloakId not found")
        }
    }


}
