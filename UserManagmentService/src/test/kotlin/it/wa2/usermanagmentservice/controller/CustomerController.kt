package it.wa2.usermanagmentservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.services.CustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import java.util.*
import kotlin.test.Test
import io.mockk.*
import it.wa2.usermanagmentservice.advices.CustomerDuplicate
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

import it.wa2.usermanagmentservice.advices.CustomerNotFound
import org.springframework.test.web.servlet.*


@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerMockMvcTest: IntegrationTest()  {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var customerService: CustomerService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private fun createValidCustomerDTO() = CustomerDTO(
            genericUserData = GenericUserDTO(
                id = 1L,
                name = "John",
                surname = "Doe",
                email = "john.doe@example.com",
                phone = "1234567890",
                address = "123 Main St",
                city = "New York"
            ),
            dateOfBirth = Date(),
            reliabilityScores = 5,
            drivingLicence = "DL123456",
            expirationDate = Date(),
            id = 0
        )

        private fun createValidCustomersList() = listOf(
            createValidCustomerDTO(),
            CustomerDTO(
                genericUserData = GenericUserDTO(
                    id = 2L,
                    name = "Jane",
                    surname = "Smith",
                    email = "jane.smith@example.com",
                    phone = "9876543210",
                    address = "456 Oak St",
                    city = "Boston"
                ),
                dateOfBirth = Date(),
                reliabilityScores = 4,
                drivingLicence = "DL789012",
                expirationDate = Date(),
                id=0
            )
        )
    }

    @Nested
    @DisplayName("GET /api/v1/customers")
    inner class GetCustomers {

        @Test
        fun `should return empty page when no customers exist`() {
            // Given
            val emptyPage = PageImpl(emptyList<CustomerDTO>(), PageRequest.of(0, 20), 0)
            every { customerService.getCustomers(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyPage

            // When/Then
            mockMvc.get("/api/v1/customers") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    jsonPath("$.content") { isArray() }
                    jsonPath("$.content") { isEmpty() }
                    jsonPath("$.totalElements") { value(0) }
                    jsonPath("$.totalPages") { value(0) }
                    jsonPath("$.number") { value(0) }
                }
            }.andDo { print() }
        }

        @Test
        fun `should return page with customers when customers exist`() {
            // Given
            val customers = createValidCustomersList()
            val customersPage = PageImpl(customers, PageRequest.of(0, 20), customers.size.toLong())
            every { customerService.getCustomers(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns customersPage

            // When/Then
            mockMvc.get("/api/v1/customers") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    jsonPath("$.content") { isArray() }
                    jsonPath("$.content.length()") { value(2) }
                    jsonPath("$.content[0].genericUserData.id") { value(1) }
                    jsonPath("$.content[0].genericUserData.name") { value("John") }
                    jsonPath("$.content[1].genericUserData.id") { value(2) }
                    jsonPath("$.content[1].genericUserData.name") { value("Jane") }
                    jsonPath("$.totalElements") { value(2) }
                }
            }.andDo { print() }
        }

        @Test
        fun `should filter customers by name`() {
            // Given
            val customer = createValidCustomerDTO()
            val customersPage = PageImpl(listOf(customer), PageRequest.of(0, 20), 1)
            every { customerService.getCustomers(any(), eq("John"), any(), any(), any(), any(), any(), any(), any()) } returns customersPage

            // When/Then
            mockMvc.get("/api/v1/customers?name=John") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content {
                    jsonPath("$.content[0].genericUserData.name") { value("John") }
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{userId}")
    inner class GetCustomerById {

        @Test
        fun `should return customer when exists`() {
            // Given
            val customer = createValidCustomerDTO()
            every { customerService.getCustomerById(1L) } returns customer

            // When/Then
            mockMvc.get("/api/v1/customers/1") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content {
                    jsonPath("$.genericUserData.id") { value(1) }
                    jsonPath("$.genericUserData.name") { value("John") }
                }
            }
        }

        @Test
        fun `should return 404 when customer not found`() {
            // Given
            every { customerService.getCustomerById(99L) } throws CustomerNotFound("Customer not found")

            // When/Then
            mockMvc.get("/api/v1/customers/99") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers")
    inner class AddCustomer {

        @Test
        fun `should create new customer successfully`() {
            // Given
            val customerDTO = createValidCustomerDTO()
            every { customerService.addCustomer(any()) } returns customerDTO

            // When/Then
            mockMvc.post("/api/v1/customers") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(customerDTO)
            }.andExpect {
                status { isCreated() }
                header { exists("Location") }
                content {
                    jsonPath("$.genericUserData.id") { value(1) }
                    jsonPath("$.genericUserData.name") { value("John") }
                }
            }
        }

        @Test
        fun `should return 400 when customer data is invalid`() {
            // Given
            val invalidCustomer = CustomerDTO(
                genericUserData = GenericUserDTO(
                    id = 1L,
                    name = "", // nome vuoto - invalido
                    surname = "Doe",
                    email = "invalid-email", // email invalida
                    phone = "1234567890",
                    address = "123 Main St",
                    city = "New York"
                ),
                dateOfBirth = Date(),
                reliabilityScores = 5,
                drivingLicence = "DL123456",
                expirationDate = Date(),
                id=0
            )

            // When/Then
            mockMvc.post("/api/v1/customers") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(invalidCustomer)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 409 when customer already exists`() {
            // Given
            val customerDTO = createValidCustomerDTO()
            every { customerService.addCustomer(any()) } throws CustomerDuplicate("Customer already exists")

            // When/Then
            mockMvc.post("/api/v1/customers") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(customerDTO)
            }.andExpect {
                status { isConflict() }
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{userId}")
    inner class UpdateCustomer {

        @Test
        fun `should update customer successfully`() {
            // Given
            val customerDTO = createValidCustomerDTO()
            every { customerService.updateCustomer(1L, any()) } returns customerDTO

            // When/Then
            mockMvc.put("/api/v1/customers/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(customerDTO)
            }.andExpect {
                status { isOk() }
                content {
                    jsonPath("$.genericUserData.id") { value(1) }
                    jsonPath("$.genericUserData.name") { value("John") }
                }
            }
        }

        @Test
        fun `should return 404 when updating non-existent customer`() {
            // Given
            val customerDTO = createValidCustomerDTO()
            every { customerService.updateCustomer(99L, any()) } throws CustomerNotFound("Customer not found")

            // When/Then
            mockMvc.put("/api/v1/customers/99") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(customerDTO)
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{userId}")
    inner class DeleteCustomer {

        @Test
        fun `should delete customer successfully`() {
            // Given
            every { customerService.deleteCustomerById(1L) } returns Unit

            // When/Then
            mockMvc.delete("/api/v1/customers/1") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status() { isOk() }
            }
        }

        @Test
        fun `should return 404 when deleting non-existent customer`() {
            // Given
            every { customerService.deleteCustomerById(99L) } throws CustomerNotFound("Customer not found")

            // When/Then
            mockMvc.delete("/api/v1/customers/99") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{userId}/eligibility")
    inner class GetEligibility {

        @Test
        fun `should return eligibility status when customer exists`() {
            // Given
            every { customerService.getEligibilityById(1L) } returns true

            // When/Then
            mockMvc.get("/api/v1/customers/1/eligibility") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { string("true") }
            }
        }

        @Test
        fun `should return 404 when checking eligibility for non-existent customer`() {
            // Given
            every { customerService.getEligibilityById(99L) } throws CustomerNotFound("Customer not found")

            // When/Then
            mockMvc.get("/api/v1/customers/99/eligibility") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }
        }
    }
}