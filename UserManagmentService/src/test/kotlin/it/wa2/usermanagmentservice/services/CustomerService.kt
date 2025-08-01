package it.wa2.usermanagmentservice.services

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.CustomerDuplicate
import it.wa2.usermanagmentservice.advices.CustomerNotFound
import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.repositories.CustomerRepository
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.util.Date


@SpringBootTest
class CustomerServiceImplTest : IntegrationTest() {

    @MockkBean
    lateinit var customerRepository: CustomerRepository

    @MockkBean
    lateinit var genericUserRepository: GenericUserRepository

    @Autowired
    lateinit var service: CustomerServiceImpl

    private val userDto = GenericUserDTO(
        id = 1,
        name = "John",
        surname = "Doe",
        email = "john@example.com",
        phone = "123-456-7890",
        address = "123 St",
        city = "City"
    )
    private val dob = Date(System.currentTimeMillis() - 20L * 365 * 24 * 60 * 60 * 1000)
    private val exp = Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
    private val customerDto = CustomerDTO(id = 1, genericUserData = userDto, dateOfBirth = dob, reliabilityScores = 5, drivingLicence = "DL123", expirationDate = exp)
    private val customerEntity = customerDto.toEntity().apply { genericUserData.id = 1L; id = 1L }

    @Test
    fun `addCustomer duplicates driving license`() {
        every { customerRepository.findByIdOrNull(customerDto.genericUserData.id) } returns null
        every { customerRepository.existsByDrivingLicense(customerDto.drivingLicence) } returns true
        every { genericUserRepository.existsByEmail(customerDto.genericUserData.email) } returns false
        every { genericUserRepository.existsByPhone(customerDto.genericUserData.phone) } returns false

        assertThrows<CustomerDuplicate> {
            service.addCustomer(customerDto)
        }
    }

    @Test
    fun `getEligibilityById returns true for eligible`() {
        every { customerRepository.findByIdOrNull(1) } returns customerEntity
        assertTrue(service.getEligibilityById(1))
    }

    @Test
    fun `getEligibilityById throws when not found`() {
        every { customerRepository.findByIdOrNull(1) } returns null
        assertThrows(CustomerNotFound::class.java) { service.getEligibilityById(1) }
    }

    @Test
    fun `deleteCustomerById throws when not exists`() {
        every { customerRepository.existsById(2) } returns false
        assertThrows(CustomerNotFound::class.java) { service.deleteCustomerById(2) }
    }
}

