package it.wa2.usermanagmentservice.services

import it.wa2.usermanagmentservice.dtos.EmployeeDTO
import it.wa2.usermanagmentservice.dtos.RoleDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EmployeeService {
    fun getEmployees(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        nameRole: String?,
        salary: Double?
    ): Page<EmployeeDTO>
    fun getEmployeeById(userId: Long): EmployeeDTO
    fun addEmployee(employee: EmployeeDTO): EmployeeDTO
    fun updateEmployeeById(userId: Long, employeeDTO: EmployeeDTO): EmployeeDTO
    fun deleteEmployee(userId: Long)
    fun getEmployeesRoles(): List<RoleDTO>
}