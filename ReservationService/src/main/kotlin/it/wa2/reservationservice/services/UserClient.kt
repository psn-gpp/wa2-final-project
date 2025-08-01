package it.wa2.reservationservice.services

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "user-management-service", url = "\${user.service.url}")
interface UserClient {
    @GetMapping("/api/v1/users/{userId}")
    fun getUserById(@PathVariable userId: Long)
}