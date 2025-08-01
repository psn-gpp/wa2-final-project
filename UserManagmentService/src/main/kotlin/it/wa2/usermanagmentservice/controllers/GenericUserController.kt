package it.wa2.usermanagmentservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.services.GenericUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


/**
 * GET all users -> [getUsers]
 *
 * GET single user -> [getUserById]
 *
 * UPDATE user -> [updateUserById]
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
class GenericUserController(private val genericUserService: GenericUserService) {

    private val logger = LoggerFactory.getLogger(GenericUserController::class.java)

    @Operation(
        summary = "Get all users",
        description = "Returns all users (customers, staffs, managers)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager')")
    @GetMapping("")
    fun getUsers(
        pageable: Pageable,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?,
        @RequestParam(required = false) address: String?,
        @RequestParam(required = false) city: String?,
    ): Page<GenericUserDTO> {
        logger.info("Requesting all user with filters:" +
                "name: $name, surname=$surname, address=$address, city=$city")
        val result = genericUserService.getUsers(
            pageable,
            name,
            surname,
            address,
            city,
        )
        logger.debug("Retrieved ${result.totalElements} users")
        return result
    }

    @Operation(
        summary = "Get a specific user by id",
        description = "Returns the user specified by id",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable("userId") userId: Long,
        request: HttpServletRequest
    ): GenericUserDTO {
        logger.info("Requesting user with id: $userId")
        //log per debug purpose
        val headers = request.headerNames
        while (headers.hasMoreElements()) {
            val name = headers.nextElement()
            val value = request.getHeader(name)
            logger.debug("Header: $name = $value")
        }

        val user = genericUserService.getUserById(userId)
        logger.debug("Retrieve user: ${user.email}")
        return user
    }

    @Operation(
        summary = "Get a specific user by keycloak id",
        description = "Returns the user specified by keycloak id",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @GetMapping("/keycloak/{keycloakId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserByKeycloakId(
        @PathVariable("keycloakId") keycloakId: String,
        request: HttpServletRequest
    ): GenericUserDTO {
        logger.info("Requesting user with id: $keycloakId")
        //log per debug purpose
        /*val headers = request.headerNames
        while (headers.hasMoreElements()) {
            val name = headers.nextElement()
            val value = request.getHeader(name)
            logger.debug("Header: $name = $value")
        }*/

        val user = genericUserService.getUserByKeycloakId(keycloakId)
        logger.debug("Retrieve user: ${user.email}")
        return user
    }


    @Operation(
        summary = "Update a user",
        description = "Update general information about a user",
    )
    @PutMapping("/{userId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    fun updateUserById(@Valid @PathVariable("userId") userId: Long, @RequestBody user: GenericUserDTO): GenericUserDTO {
        logger.info("Updating user with id: $userId")
        val updatedUser = genericUserService.updateUserById(userId, user)
        logger.info("User updated successfully with id: $userId")
        return updatedUser
    }



}
