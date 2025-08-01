package it.wa2.usermanagmentservice.enpoints

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.*
import it.wa2.usermanagmentservice.dtos.*
import it.wa2.usermanagmentservice.services.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int
)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GenericUserControllerTest : IntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockkBean
    private lateinit var genericUserService: GenericUserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun baseUrl() = "http://localhost:$port/api/v1/users"

    companion object {
        private fun createValidUserDTO(id: Long = 1) = GenericUserDTO(
            id = id,
            name = "John",
            surname = "Doe",
            email = "john.doe@example.com",
            phone = "1234567890",
            address = "123 Main St",
            city = "Rome"
        )

        private fun createPageOfUsers(list: List<GenericUserDTO>) = PageImpl(
            list,
            PageRequest.of(0, 10),
            list.size.toLong()
        )
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    inner class GetUsers {
        @Test
        fun `should return empty page when no users exist`() {
            every { genericUserService.getUsers(any(), any(), any(), any(), any()) } returns createPageOfUsers(emptyList())

            val response = restTemplate.exchange(
                baseUrl() + "?page=0&size=10",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<PageResponse<GenericUserDTO>>() {}
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body!!.content.isEmpty())
        }

        @Test
        fun `should return page with users when users exist`() {
            val list = listOf(
                createValidUserDTO(1),
                createValidUserDTO(2).copy(name = "Jane")
            )
            every { genericUserService.getUsers(any(), any(), any(), any(), any()) } returns createPageOfUsers(list)

            val response = restTemplate.exchange(
                baseUrl() + "?page=0&size=10",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<PageResponse<GenericUserDTO>>() {}
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(2, response.body!!.content.size)
            assertEquals("Jane", response.body!!.content[1].name)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}")
    inner class GetUserById {
        @Test
        fun `should return user when exists`() {
            val user = createValidUserDTO(1)
            every { genericUserService.getUserById(1) } returns user

            val response = restTemplate.getForEntity(
                "${baseUrl()}/1", GenericUserDTO::class.java
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(1, response.body!!.id)
        }

        @Test
        fun `should return 404 when user not found`() {
            every { genericUserService.getUserById(99) } throws GenericUserNotFound("Not found")

            val response = restTemplate.getForEntity(
                "${baseUrl()}/99", String::class.java
            )

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{userId}")
    inner class UpdateUser {
        @Test
        fun `should update user successfully`() {
            val dto = createValidUserDTO(1).copy(name = "Johnny")
            every { genericUserService.updateUserById(1, any()) } returns dto

            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(objectMapper.writeValueAsString(dto), headers)
            val response = restTemplate.exchange(
                "${baseUrl()}/1",
                HttpMethod.PUT,
                request,
                GenericUserDTO::class.java
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals("Johnny", response.body!!.name)
        }

        @Test
        fun `should return 400 on id mismatch`() {
            val dto = createValidUserDTO(2)
            every { genericUserService.updateUserById(1, any()) } throws GenericUserInconsistency("Mismatch")

            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(objectMapper.writeValueAsString(dto), headers)
            val response = restTemplate.exchange(
                "${baseUrl()}/1",
                HttpMethod.PUT,
                request,
                String::class.java
            )

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        }
    }
}


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerTest : IntegrationTest() {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var restTemplate: TestRestTemplate
    @MockkBean private lateinit var employeeService: EmployeeService
    @Autowired private lateinit var objectMapper: ObjectMapper

    private fun baseUrl() = "http://localhost:$port/api/v1/employees"

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
            val page = PageImpl(emptyList<EmployeeDTO>(), PageRequest.of(0,10),0)
            every { employeeService.getEmployees(any(), any(), any(), any(), any(), any(), any()) } returns page

            val response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<PageImpl<EmployeeDTO>>() {}
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(0, response.body!!.totalElements)
        }

        @Test
        fun `non-empty page`() {
            val list = listOf(createValidEmployeeDTO())
            val page = PageImpl(list, PageRequest.of(0,10),1)
            every { employeeService.getEmployees(any(), any(), any(), any(), any(), any(), any()) } returns page

            val response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<PageImpl<EmployeeDTO>>() {}
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(3000.0, response.body!!.content[0].salary)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{userId}")
    inner class GetEmployeeById {
        @Test
        fun `exists`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.getEmployeeById(1) } returns dto

            val response = restTemplate.getForEntity(
                "${baseUrl()}/1", EmployeeDTO::class.java
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(3000.0, response.body!!.salary)
        }

        @Test
        fun `not found`() {
            every { employeeService.getEmployeeById(99) } throws EmployeeNotFound("No emp")
            val response = restTemplate.getForEntity(
                "${baseUrl()}/99", String::class.java
            )
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    inner class AddEmployee {
        @Test
        fun `create success`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.addEmployee(any()) } returns dto

            val request = HttpEntity(objectMapper.writeValueAsString(dto))
            val response = restTemplate.postForEntity(
                baseUrl(),
                request,
                EmployeeDTO::class.java
            )

            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertNotNull(response.headers.location)
            assertEquals(1, response.body!!.id)
        }

        @Test
        fun `conflict on duplicate`() {
            val dto = createValidEmployeeDTO()
            every { employeeService.addEmployee(any()) } throws GenericUserDuplicate("dup")
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(objectMapper.writeValueAsString(dto),headers)
            val response = restTemplate.postForEntity(
                baseUrl(),
                request,
                String::class.java
            )
            assertEquals(HttpStatus.CONFLICT, response.statusCode)
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{userId}")
    inner class UpdateEmployee {
        @Test
        fun `update success`() {
            val dto = createValidEmployeeDTO().copy(salary = 3500.0)
            every { employeeService.updateEmployeeById(1, any()) } returns dto
            val request = HttpEntity(objectMapper.writeValueAsString(dto))
            val response = restTemplate.exchange(
                "${baseUrl()}/1",
                HttpMethod.PUT,
                request,
                EmployeeDTO::class.java
            )
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(3500.0, response.body!!.salary)
        }

        @Test
        fun `not found`() {
            every { employeeService.updateEmployeeById(99, any()) } throws EmployeeNotFound("no")
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(objectMapper.writeValueAsString(createValidEmployeeDTO()),headers)
            val response = restTemplate.exchange(
                "${baseUrl()}/99",
                HttpMethod.PUT,
                request,
                String::class.java
            )
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{userId}")
    inner class DeleteEmployee {
        @Test
        fun `delete success`() {
            every { employeeService.deleteEmployee(1) } returns Unit
            val response = restTemplate.exchange(
                "${baseUrl()}/1",
                HttpMethod.DELETE,
                null,
                String::class.java
            )
            assertEquals(HttpStatus.OK, response.statusCode)
        }

        @Test
        fun `not found`() {
            every { employeeService.deleteEmployee(99) } throws EmployeeNotFound("no")
            val response = restTemplate.exchange(
                "${baseUrl()}/99",
                HttpMethod.DELETE,
                null,
                String::class.java
            )
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/roles")
    inner class GetRoles {
        @Test
        fun `list roles`() {
            val roles = listOf(createValidRoleDTO())
            every { employeeService.getEmployeesRoles() } returns roles
            val response = restTemplate.exchange(
                "${baseUrl()}/roles",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<RoleDTO>>() {}
            )
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals("Staff", response.body!![0].nameRole)
        }
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest : IntegrationTest() {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var restTemplate: TestRestTemplate
    @MockkBean private lateinit var customerService: CustomerService

    private fun baseUrl() = "http://localhost:$port/api/v1/customers"

    companion object {
        private fun createValidCustomerDTO() = CustomerDTO(
            id = 1,
            genericUserData = GenericUserDTO(1, "John", "Doe", "john.doe@example.com", "1234567890", "123 Main", "Rome"),
            dateOfBirth = Date(),
            reliabilityScores = 5,
            drivingLicence = "DL123",
            expirationDate = Date()
        )
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{userId}/eligibility")
    inner class GetEligibility {
        @Test fun `eligibility true`() {
            every { customerService.getEligibilityById(1) } returns true
            val response = restTemplate.getForEntity(
                "${baseUrl()}/1/eligibility",
                String::class.java
            )
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals("true", response.body)
        }

        @Test fun `not found`() {
            every { customerService.getEligibilityById(99) } throws CustomerNotFound("no")
            val response = restTemplate.getForEntity(
                "${baseUrl()}/99/eligibility",
                String::class.java
            )
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        }
    }
}
