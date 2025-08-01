package it.wa2.usermanagmentservice.services

import it.wa2.usermanagmentservice.advices.*
import it.wa2.usermanagmentservice.dtos.EmployeeDTO
import it.wa2.usermanagmentservice.dtos.RoleDTO
import it.wa2.usermanagmentservice.repositories.EmployeeRepository
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import it.wa2.usermanagmentservice.repositories.RoleRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated


/**
 * [getEmployees]: return all employees
 *
 * [getEmployeeById]: return the employee with a specified id
 *
 * [addEmployee]: add an employee
 *
 * [updateEmployeeById]: update an employee
 *
 * [deleteEmployee]: delete an employee
 *
 * [getEmployeesRoles]: return all available roles
 */
@Service
@Primary
@Validated
@Transactional
class EmployeeServiceImpl(private val employeeRepository: EmployeeRepository, private val genericUserRepository: GenericUserRepository, private val roleRepository: RoleRepository) : EmployeeService {

    private val logger = LoggerFactory.getLogger(EmployeeServiceImpl::class.java)

    private fun checkDuplicate(employeeDTO: EmployeeDTO) {
        logger.debug("Checking for duplicates: ${employeeDTO.genericUserData.email}")
        val currentEmployee = employeeRepository.findByIdOrNull(employeeDTO.genericUserData.id)

        if(currentEmployee?.genericUserData?.email != employeeDTO.genericUserData.email && genericUserRepository.existsByEmail(employeeDTO.genericUserData.email)) {
            logger.warn("Duplicate email found: ${employeeDTO.genericUserData.email}")
            throw EmployeeDuplicate("An employee with email ${employeeDTO.genericUserData.email} already exists.")
        }
        /*if(currentEmployee?.genericUserData?.phone != employeeDTO.genericUserData.phone && genericUserRepository.existsByPhone(employeeDTO.genericUserData.phone)) {
            logger.warn("Duplicate phone found: ${employeeDTO.genericUserData.phone}")
            throw EmployeeDuplicate("An employee with phone ${employeeDTO.genericUserData.phone} already exists.")
        }*/
    }


    /**
     * Return all employees
     */
    override fun getEmployees(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        nameRole: String?,
        salary: Double?
    ): Page<EmployeeDTO> {
        logger.debug("Fetching employees with filters")
        return employeeRepository.findWithFilters(
            pageable,
            name,
            surname,
            address,
            city,
            nameRole,
            salary
        ).map { it.toDTO() }
    }

    /**
     *  Return the employee with a specified id or throw [EmployeeNotFound] exception
     */
    override fun getEmployeeById(userId: Long): EmployeeDTO {
        logger.debug("Fetching employee with ID: $userId")
        return employeeRepository.findByIdOrNull(userId)?.toDTO()
            ?: run {
                logger.warn("Employee not found with id: $userId")
                throw EmployeeNotFound("Employee with id $userId not found")
            }
    }

    /**
     * before to add an employee:
     *
     * - performs a check about duplicate
     *
     * - checks the exists of the role, if no add the role
     *
     */
    override fun addEmployee(employee: EmployeeDTO): EmployeeDTO {
        logger.info("Adding new employee: ${employee.genericUserData.email}")
        checkDuplicate(employee)
        val role = roleRepository.findFirstByNameRole(employee.role.nameRole) ?: run {
            roleRepository.save(employee.role.toEntity())
        }
        val savedEmployee = employeeRepository.save(employee.toEntity(role)).toDTO()
        logger.info("Employee added successfully with ID: ${savedEmployee.genericUserData.id}")
        return savedEmployee
    }

    /**
     * Performs the update of specify employee and return the updated one
     *
     * If [employeeDTO] has an id different to [userId] throw [EmployeeInconsistency] exception
     *
     * If [userId] is not present throw [EmployeeNotFound] exception
     *
     * If [employeeDTO] has a role that is not present throw [RoleNotFound] exception
     *
     */
    override fun updateEmployeeById(userId: Long, employeeDTO: EmployeeDTO): EmployeeDTO {
        logger.info("Updating employee with ID: $userId")
        if (userId != employeeDTO.genericUserData.id) {
            logger.error("id mismatch: path id=$userId, body id=${employeeDTO.genericUserData.id}")
            throw EmployeeInconsistency("Inconsistent employee with id ${employeeDTO.genericUserData.id}")
        }
        checkDuplicate(employeeDTO)
        val role = roleRepository.findFirstByNameRole(employeeDTO.role.nameRole) ?: run {
            logger.error("Role ${employeeDTO.role.nameRole} not found")
            throw RoleNotFound("Role with nameRole ${employeeDTO.role.nameRole} not found")
        }
        val employee = employeeRepository.findByIdOrNull(employeeDTO.id) ?: throw EmployeeNotFound("Employee with id ${employeeDTO.id}")
        employee.update(employeeDTO.toEntity(role))

        val updatedEmployee = employeeRepository.save(employee).toDTO()
        logger.info("Employee $userId updated successfully")
        return updatedEmployee
    }

    /**
     * Performs delete of specify employee
     *
     * if [userId] is not present throw [EmployeeNotFound] exception
     */
    override fun deleteEmployee(userId: Long) {
        logger.info("Attempting to delete employee with id: $userId")
        if(!employeeRepository.existsById(userId)) {
            throw EmployeeNotFound("Employee with id $userId not found")
        }
        employeeRepository.deleteById(userId)
        logger.info("Employee $userId deleted successfully")
    }

    /**
     * Return all available roles
     */
    override fun getEmployeesRoles(): List<RoleDTO> {
        logger.debug("Fetching roles")
        return roleRepository.findAll().map { it.toDTO() }
    }

}