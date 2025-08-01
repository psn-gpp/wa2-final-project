package it.wa2.usermanagmentservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.usermanagmentservice.dtos.EmployeeDTO
import it.wa2.usermanagmentservice.dtos.RoleDTO
import it.wa2.usermanagmentservice.services.EmployeeService
import it.wa2.usermanagmentservice.services.GenericUserService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder


/**
 * Get all employees: [getEmployees]
 *
 * Get one employee: [getEmployeeById]
 *
 * add: [addEmployee]
 *
 * delete: [deleteEmployeeById]
 *
 * put: [updateEmployeeById]
 *
 *  ## SERVE ???????
 *
 * get role employees:  ***SERVE ????*** ---> No !!!!! GET allRole
 */


@RestController
@RequestMapping("/api/v1/employees")
@Validated
class EmployeeController(
    private val employeeService: EmployeeService,
    private val genericUserService: GenericUserService
) {

    private val logger = LoggerFactory.getLogger(EmployeeController::class.java)

    @Operation(
        summary = "Get all employees",
        description = "Returns all employees",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager')")
    @GetMapping("")
    fun getEmployees(
        pageable: Pageable,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?,
        @RequestParam(required = false) address: String?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) nameRole: String?,
        @RequestParam(required = false) salary: Double?,
    ): Page<EmployeeDTO> {
        logger.info("Requesting all employees with filters:" +
                "name: $name, surname=$surname, city=$city, address=$address, salary=$salary, roleName=$nameRole")
        val result = employeeService.getEmployees(
            pageable,
            name,
            surname,
            address,
            city,
            nameRole,
            salary
        )
        logger.debug("Retrieved ${result.totalElements} employees")
        return result
    }


    @Operation(
        summary = "Get single employee",
        description = "Returns a employee",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager') or hasRole('Staff') or hasRole('Fleet_Manager')")
    @GetMapping("/{userId}")
    fun getEmployeeById(@PathVariable userId: Long): EmployeeDTO {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.authorities.any {
                logger.debug("Checking authority: ${it.authority}")
                it.authority == "ROLE_Fleet_Manager" || it.authority == "ROLE_Staff"
            }) {
            if (!checkUser(userId)) {
                throw Exception(HttpStatus.FORBIDDEN.toString())
            }
        }

        logger.info("Requesting employee with id: $userId")
        val employee = employeeService.getEmployeeById(userId)
        logger.debug("Retrieve employee:${employee.genericUserData.email}")
        return employee
    }


    @Operation(
        summary = "Add a new employee",
        description = "Creates a new employee",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PreAuthorize("hasRole('Manager')")
    @PostMapping("")
    fun addEmployee(
        @Valid @RequestBody employee: EmployeeDTO,
        uriBilder: UriComponentsBuilder
    ): ResponseEntity<EmployeeDTO> {
        logger.info("Creating a new employee with email: ${employee.genericUserData.email}")
        val newEmployee = employeeService.addEmployee(employee)
        val location = uriBilder.path("api/v1/employees/{userId}").buildAndExpand(newEmployee.genericUserData.id).toUri()
        logger.info("Employee created successfully with id: ${newEmployee.genericUserData.id}")
        return ResponseEntity.created(location).body(newEmployee)
    }




    @Operation(
        summary = "Update an employee",
        description = "Update an employee",
    )
    @PreAuthorize("hasRole('Manager') or hasRole('Staff') or hasRole('Fleet_Manager')")
    @PutMapping("/{userId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    fun updateEmployeeById(@Valid @PathVariable("userId") userId: Long, @RequestBody employee: EmployeeDTO): EmployeeDTO {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.authorities.any {
                logger.debug("Checking authority: ${it.authority}")
                it.authority == "ROLE_Fleet_Manager" || it.authority == "ROLE_Staff"
            }) {
            if (!checkUser(userId)) {
                throw Exception(HttpStatus.FORBIDDEN.toString())
            }
        }

        logger.info("Updating employee with id: $userId")
        val updatedEmployee =  employeeService.updateEmployeeById(userId, employee)
        logger.info("Employee updated successfully with id: $userId")
        return updatedEmployee
    }


    @Operation(
        summary = "Delete an employee",
        description = "Delete an employee by id",
    )
    @PreAuthorize("hasRole('Manager')")
    @DeleteMapping("/{userId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    fun deleteEmployeeById(@PathVariable("userId") userId: Long) {
        logger.info("Deleting employee with id: $userId")
        employeeService.deleteEmployee(userId)
        logger.info("Deleted employee with id: $userId")
    }


    @Operation(
        summary = "Get all available roles",
        description = "Returns all available roles",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Manager')")
    @GetMapping("/roles")
    fun getEmployeesRoles(): List<RoleDTO> {
        logger.info("Requesting all roles")
        val roles = employeeService.getEmployeesRoles()
        logger.info("Retrieved roles: $roles")
        return roles
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