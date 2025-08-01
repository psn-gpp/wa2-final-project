package it.wa2.reservationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableFeignClients(basePackages = ["it.wa2.reservationservice.services"])
class ReservationServiceApplication/*{
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}*/

fun main(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args)
}
