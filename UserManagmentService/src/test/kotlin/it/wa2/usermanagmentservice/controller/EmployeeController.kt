package it.wa2.usermanagmentservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.*
import it.wa2.usermanagmentservice.dtos.*
import it.wa2.usermanagmentservice.services.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.*


@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerMockMvcTest : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc
    @MockkBean
    private lateinit var employeeService: EmployeeService
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private fun createValidRoleDTO() = RoleDTO(id = 1, nameRole = "Staff")
        private fun createValidUserDTO() = GenericUserDTO(
            id = 1, name = "John", surname = "Doe", email = "john@example.com",
            phone = "1234567890", address = "Addr", city = "Rome"
        )

        private fun createValidEmployeeDTO() = EmployeeDTO(
            id = 1, genericUserData = createValidUserDTO(), role = createValidRoleDTO(), salary = 3000.0
        )
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    inner class GetEmployees {
        @Test
        fun `empty page`() {
            val page = PageImpl(emptyList<EmployeeDTO>(), PageRequest.of(0, 10), 0)
            every { employeeService.getEmployees(any(), any(), any(), any(), any(), any(), any()) } returns page
            mockMvc.get("/api/v1/employees") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isOk() }; jsonPath("$.totalElements") { value(0) } }
        }

        @Test
        fun `non-empty page`() {
            val list = listOf(createValidEmployeeDTO())
            val page = PageImpl(list, PageRequest.of(0, 10), 1)
            every { employeeService.getEmployees(any(), any(), any(), any(), any(), any(), any()) } returns page
            mockMvc.get("/api/v1/employees") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isOk() }; jsonPath("$.content[0].salary") { value(3000.0) } }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{userId}")
    inner class GetEmployeeById {
        @Test
        fun `exists`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.getEmployeeById(1) } returns dto
            mockMvc.get("/api/v1/employees/1") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isOk() }; jsonPath("$.salary") { value(3000.0) } }
        }

        @Test
        fun `not found`() {
            every { employeeService.getEmployeeById(99) } throws EmployeeNotFound("No emp")
            mockMvc.get("/api/v1/employees/99") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    inner class AddEmployee {
        @Test
        fun `create success`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.addEmployee(any()) } returns dto
            mockMvc.post("/api/v1/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(dto)
            }.andExpect {
                status { isCreated() }
                header { exists("Location") }
                jsonPath("$.id") { value(1) }
            }
        }

        @Test
        fun `conflict on duplicate`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.addEmployee(any()) } throws GenericUserDuplicate("dup")
            mockMvc.post("/api/v1/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(dto)
            }.andExpect { status { isConflict() } }
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{userId}")
    inner class UpdateEmployee {
        @Test
        fun `update success`() {
            val dto = createValidEmployeeDTO().copy(salary = 3500.0)
            every { employeeService.updateEmployeeById(1, any()) } returns dto
            mockMvc.put("/api/v1/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(dto)
            }.andExpect { status { isOk() }; jsonPath("$.salary") { value(3500.0) } }
        }

        @Test
        fun `not found`() {
            every { employeeService.updateEmployeeById(99, any()) } throws EmployeeNotFound("no")
            mockMvc.put("/api/v1/employees/99") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(createValidEmployeeDTO())
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{userId}")
    inner class DeleteEmployee {
        @Test
        fun `delete success`() {
            every { employeeService.deleteEmployee(1) } returns Unit
            mockMvc.delete("/api/v1/employees/1") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isOk() } }
        }

        @Test
        fun `not found`() {
            every { employeeService.deleteEmployee(99) } throws EmployeeNotFound("no")
            mockMvc.delete("/api/v1/employees/99") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/roles")
    inner class GetRoles {
        @Test
        fun `list roles`() {
            val roles = listOf(createValidRoleDTO())
            every { employeeService.getEmployeesRoles() } returns roles
            mockMvc.get("/api/v1/employees/roles") { accept = MediaType.APPLICATION_JSON }
                .andExpect { status { isOk() }; jsonPath("$[0].nameRole") { value("Staff") } }
        }
    }
}