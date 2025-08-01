package it.wa2.apigateway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(val crr: ClientRegistrationRepository) {

    fun oidcLogoutSuccessHandler() = OidcClientInitiatedLogoutSuccessHandler(crr).also {
        it.setPostLogoutRedirectUri("http://localhost:8084/ui?logout")
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/serverLogin").authenticated()
                auth.anyRequest().permitAll()
            }
            .oauth2Login {
                it.successHandler { _, response, _ -> response.sendRedirect("http://localhost:8084/ui") }
                it.failureUrl("http://localhost:8084/ui?error=login")
            }
            .csrf {
                //it.disable()
                it.ignoringRequestMatchers( "/logout" )
            }
            .logout {
                it.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }
        return httpSecurity.build()
    }
}