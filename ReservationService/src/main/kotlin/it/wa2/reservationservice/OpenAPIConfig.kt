package it.wa2.reservationservice

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {
    @Bean
    fun getCarModelAPIDocumentation() :OpenAPI= OpenAPI()
        .info(
            Info().title("RentalCarAPI")
            .description("Using this API you can get information about the car model")
            .version("v1.0")
        )
}