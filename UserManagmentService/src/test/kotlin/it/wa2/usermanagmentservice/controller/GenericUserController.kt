package it.wa2.usermanagmentservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import it.wa2.usermanagmentservice.IntegrationTest
import it.wa2.usermanagmentservice.advices.GenericUserInconsistency
import it.wa2.usermanagmentservice.advices.GenericUserNotFound
import it.wa2.usermanagmentservice.dtos.GenericUserDTO
import it.wa2.usermanagmentservice.services.GenericUserService
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
class GenericUserControllerMockMvcTest : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var genericUserService: GenericUserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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

        private fun createValidUsersList() = listOf(
            createValidUserDTO(1),
            createValidUserDTO(2).copy(name = "Jane", email = "jane.doe@example.com")
        )
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    inner class GetUsers {
        @Test
        fun `should return empty page when no users exist`() {
            val emptyPage = PageImpl(emptyList<GenericUserDTO>(), PageRequest.of(0, 10), 0)
            every { genericUserService.getUsers(any(), any(), any(), any(), any()) } returns emptyPage

            mockMvc.get("/api/v1/users") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.content") { isArray() }
                jsonPath("$.totalElements") { value(0) }
            }
        }

        @Test
        fun `should return page with users when users exist`() {
            val list = createValidUsersList()
            val page = PageImpl(list, PageRequest.of(0, 10), list.size.toLong())
            every { genericUserService.getUsers(any(), any(), any(), any(), any()) } returns page

            mockMvc.get("/api/v1/users") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].id") { value(1) }
                jsonPath("$.content[1].name") { value("Jane") }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}")
    inner class GetUserById {
        @Test
        fun `should return user when exists`() {
            val user = createValidUserDTO(1)
            every { genericUserService.getUserById(1) } returns user

            mockMvc.get("/api/v1/users/1") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.email") { value("john.doe@example.com") }
            }
        }

        @Test
        fun `should return 404 when user not found`() {
            every { genericUserService.getUserById(99) } throws GenericUserNotFound("Not found")

            mockMvc.get("/api/v1/users/99") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{userId}")
    inner class UpdateUser {
        @Test
        fun `should update user successfully`() {
            val dto = createValidUserDTO(1).copy(name = "Johnny")
            every { genericUserService.updateUserById(1, any()) } returns dto

            mockMvc.put("/api/v1/users/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(dto)
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Johnny") }
            }
        }

        @Test
        fun `should return 400 on id mismatch`() {
            val dto = createValidUserDTO(2)
            every { genericUserService.updateUserById(1, any()) } throws GenericUserInconsistency("Mismatch")

            mockMvc.put("/api/v1/users/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(dto)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}