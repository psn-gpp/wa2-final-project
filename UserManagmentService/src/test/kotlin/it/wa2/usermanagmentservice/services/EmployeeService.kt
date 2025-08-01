package it.wa2.usermanagmentservice.services

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.EmployeeInconsistency
import it.wa2.usermanagmentservice.advices.EmployeeNotFound
import it.wa2.usermanagmentservice.dtos.EmployeeDTO
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.dtos.RoleDTO
import it.wa2.usermanagmentservice.entities.Role
import it.wa2.usermanagmentservice.repositories.EmployeeRepository
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import it.wa2.usermanagmentservice.repositories.RoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull


@SpringBootTest
class EmployeeServiceImplTest : IntegrationTest() {

    @MockkBean
    lateinit var employeeRepository: EmployeeRepository

    @MockkBean
    lateinit var genericUserRepository: GenericUserRepository

    @MockkBean
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var service: EmployeeServiceImpl

    private val userDto = GenericUserDTO(
        id = 1,
        name = "John",
        surname = "Doe",
        email = "john@example.com",
        phone = "123-456-7890",
        address = "123 St",
        city = "City"
    )
    private val roleDto = RoleDTO(id = 0, nameRole = "Staff")
    private val roleEntity = roleDto.toEntity().apply { id = 1L }
    private val employeeDto = EmployeeDTO(id = 1, genericUserData = userDto, role = roleDto, salary = 5000.0)
    private val employeeEntity = employeeDto.toEntity(roleEntity).apply { id = 1L }

    @Test
    fun `getEmployeeById returns DTO when exists`() {
        every { employeeRepository.findByIdOrNull(1) } returns employeeEntity

        val result = service.getEmployeeById(1)

        assertEquals(5000.0, result.salary)
    }

    @Test
    fun `getEmployeeById throws when not found`() {
        every { employeeRepository.findByIdOrNull(1) } returns null

        assertThrows(EmployeeNotFound::class.java) { service.getEmployeeById(1) }
    }

    @Test
    fun `addEmployee saves new role when not exists`() {
        every { employeeRepository.findByIdOrNull(any()) } returns employeeEntity
        every { genericUserRepository.existsByEmail(userDto.email) } returns false
        every { genericUserRepository.existsByPhone(userDto.phone) } returns false
        every { roleRepository.findFirstByNameRole(roleDto.nameRole) } returns null
        every { roleRepository.save(any()) } returns roleEntity
        every { employeeRepository.save(any()) } returns employeeEntity

        val result = service.addEmployee(employeeDto)

        verify { roleRepository.save(any<Role>()) }
        assertEquals(employeeDto.salary, result.salary)
    }

    @Test
    fun `updateEmployeeById throws inconsistency when id mismatch`() {
        assertThrows(EmployeeInconsistency::class.java) {
            service.updateEmployeeById(2, employeeDto)
        }
    }
}
