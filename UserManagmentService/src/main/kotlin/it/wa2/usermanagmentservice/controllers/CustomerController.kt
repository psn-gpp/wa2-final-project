package it.wa2.usermanagmentservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.services.CustomerService
import it.wa2.usermanagmentservice.services.GenericUserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException.Forbidden
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

/**
 * Get all customer: [getCustomers]
 *
 * Get one customer: [getCustomerById]
 *
 * add: [addCustomer]
 *
 * delete: [deleteCustomerById]
 *
 * put: [updateCustomerById]
 *
 * get eligibility: [getEligibilityById]
 */


@RestController
@RequestMapping("/api/v1/customers")
@Validated
class CustomerController(
    private val customerService: CustomerService,
    private val genericUserService: GenericUserService
) {

    private val logger = LoggerFactory.getLogger(CustomerController::class.java)

    @Operation(
        summary = "Get all customers",
        description = "Returns all customers",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager')")
    @GetMapping("")
    fun getCustomers(
        pageable: Pageable,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?,
        @RequestParam(required = false) address: String?,
        @RequestParam(required = false) city: String?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false) dateOfBirth: Date?,
        @RequestParam(required = false) reliabilityScores: Int?,
        @RequestParam(required = false) drivingLicence: String?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false) expirationDate: Date?,
    ): Page<CustomerDTO> {
        logger.info(
            "Requesting all customers with filters:" +
                    "name: $name, surname=$surname, city=$city, dateOfBirth=$dateOfBirth " +
                    "reliabilityScores=$reliabilityScores, drivingLicence=$drivingLicence expirationDate=$expirationDate"
        )
        val result = customerService.getCustomers(
            pageable,
            name,
            surname,
            address,
            city,
            dateOfBirth,
            reliabilityScores,
            drivingLicence,
            expirationDate
        )
        logger.debug("Retrieved ${result.totalElements} customers")
        return result
    }


    @Operation(
        summary = "Get single customer",
        description = "Returns a customer",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager') or hasRole('Customer')")
    @GetMapping("/{userId}")
    fun getCustomerById(@PathVariable userId: Long): CustomerDTO {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.authorities.any {
                logger.debug("Checking authority: ${it.authority}")
                it.authority == "ROLE_Customer"
            }) {
            if (!checkUser(userId)) {
                throw Exception(HttpStatus.FORBIDDEN.toString())
            }
        }

        logger.info("Requesting customer with id: $userId")
        val customer = customerService.getCustomerById(userId)
        logger.debug("Retrieve customer:${customer.genericUserData.email}")
        return customer
    }


    @Operation(
        summary = "Add a new customer",
        description = "Creates a new customer",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PreAuthorize("hasRole('Customer')")
    @PostMapping("")
    fun addCustomer(
        @Valid @RequestBody customer: CustomerDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<CustomerDTO> {
        logger.info("Creating a new customer with email: ${customer.genericUserData.email}")
        val newCustomer = customerService.addCustomer(customer)
        val location =
            uriBuilder.path("api/v1/customers/{userId}").buildAndExpand(newCustomer.genericUserData.id).toUri()
        logger.info("Customer created successfully with id: ${newCustomer.genericUserData.id}")
        return ResponseEntity.created(location).body(newCustomer)
    }


    @Operation(
        summary = "Update a customer",
        description = "Update a customer",
    )
    @PreAuthorize("hasRole('Customer')")
    @PutMapping("/{userId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    fun updateCustomerById(
        @Valid @PathVariable("userId") userId: Long,
        @RequestBody customer: CustomerDTO
    ): CustomerDTO {


        if (!checkUser(userId)) {
            throw Exception(HttpStatus.FORBIDDEN.toString())
        }

        logger.info("Updating customer with id: $userId")
        val updatedCustomer = customerService.updateCustomer(userId, customer)
        logger.info("Customer updated successfully with id: $userId")
        return updatedCustomer
    }


    @Operation(
        summary = "Get customer eligibility",
        description = "Returns eligibility",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Customer')")
    @GetMapping("/{userId}/eligibility")
    fun getEligibilityById(@Valid @PathVariable("userId") userId: Long): Boolean {
        logger.info("Checking eligibility for customer with id: $userId")
        val eligibility = customerService.getEligibilityById(userId)
        logger.info("Eligibility result for customer with id: $userId: $eligibility")
        return eligibility
    }


    @Operation(
        summary = "Delete a customer",
        description = "Delete a customer by id",
    )
    @PreAuthorize("hasRole('Customer') or hasRole('Manager')")
    @DeleteMapping("/{userId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    fun deleteCustomerById(@PathVariable("userId") userId: Long) {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.authorities.any {
                logger.debug("Checking authority: ${it.authority}")
                it.authority == "ROLE_Customer"
            }) {
            if (!checkUser(userId)) {
                throw Exception(HttpStatus.FORBIDDEN.toString())
            }
        }

        logger.info("Deleting customer with id: $userId")
        customerService.deleteCustomerById(userId)
        logger.info("Deleted customer with id: $userId")
    }


    private fun checkUser(userId: Long): Boolean {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication

        if (authentication.principal !is Jwt) return false

        val jwt: Jwt = authentication.principal as Jwt

        val jwtId = jwt.getClaim<String>("sub")
        logger.debug("JWT ID: $jwtId")
        val user = genericUserService.getUserByKeycloakId(jwtId)
        logger.debug("User from JWT: ${user.name} ${user.surname} with ID: ${user.id}")
        return user.id == userId


    }
}

