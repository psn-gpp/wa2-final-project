package it.wa2.usermanagmentservice.services

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.GenericUserInconsistency
import it.wa2.usermanagmentservice.advices.GenericUserNotFound
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.dtos.toEntity
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull


@SpringBootTest
class GenericUserServiceImplTest : IntegrationTest() {

    @MockkBean
    lateinit var genericUserRepository: GenericUserRepository

    @Autowired
    lateinit var service: UserServiceImpl

    private val sampleDto = GenericUserDTO(
        id = 1,
        name = "John",
        surname = "Doe",
        email = "john@example.com",
        phone = "123-456-7890",
        address = "123 St",
        city = "City"
    )
    private val sampleEntity = sampleDto.toEntity().apply { id = 1 }

    @Test
    fun `getUsers returns page of DTOs`() {
        val pageRequest = PageRequest.of(0, 10)
        every { genericUserRepository.findWithFilters(pageRequest, null, null, null, null) } returns PageImpl(listOf(sampleEntity))

        val result = service.getUsers(pageRequest, null, null, null, null)

        assertEquals(1, result.totalElements)
        assertEquals(sampleDto.email, result.content[0].email)
    }

    @Test
    fun `getUserById returns DTO when exists`() {
        every { genericUserRepository.findByIdOrNull(1) } returns sampleEntity

        val result = service.getUserById(1)

        assertEquals(sampleDto.email, result.email)
    }

    @Test
    fun `getUserById throws when not found`() {
        every { genericUserRepository.findByIdOrNull(1) } returns null

        assertThrows(GenericUserNotFound::class.java) { service.getUserById(1) }
    }

    @Test
    fun `updateUserById throws inconsistency when id mismatch`() {
        assertThrows(GenericUserInconsistency::class.java) {
            service.updateUserById(2, sampleDto)
        }
    }

    @Test
    fun `updateUserById updates when valid`() {
        val updatedDto = sampleDto.copy(name = "Jane")
        every { genericUserRepository.findByIdOrNull(1) } returns sampleEntity
        every { genericUserRepository.existsByEmail(updatedDto.email) } returns false
        every { genericUserRepository.existsByPhone(updatedDto.phone) } returns false
        every { genericUserRepository.save(any()) } answers { firstArg() }

        val result = service.updateUserById(1, updatedDto)

        assertEquals("Jane", result.name)
    }
}
