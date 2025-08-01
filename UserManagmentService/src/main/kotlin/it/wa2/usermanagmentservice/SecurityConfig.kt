package it.wa2.usermanagmentservice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
class SecurityConfig {
    private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(KeycloakRealmRoleConverter())
        return converter
    }
    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/users/**").hasAnyRole("Fleet_Manager", "Manager", "Staff", "Customer")
                it.requestMatchers("/api/v1/employees/**").hasAnyRole("Fleet_Manager", "Manager", "Staff", "Customer")
                it.requestMatchers("/api/v1/customers/**").hasAnyRole("Fleet_Manager", "Manager", "Staff", "Customer")
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt-> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())}
            }
            .build()
    }
}
