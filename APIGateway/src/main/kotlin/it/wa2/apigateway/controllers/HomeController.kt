package it.wa2.apigateway.controllers

import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/")
    fun home(httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect("/ui")
    }

    @GetMapping("/serverLogin")
    fun login(httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect("/ui")
    }

    @GetMapping("/me")
    fun me(authentication: Authentication?, csrfToken: CsrfToken): Map<String, Any?> {
        if (authentication!=null) {
            val user = authentication.principal as OidcUser
            return mapOf(
                "name" to user.preferredUsername,
                "userInfo" to user.userInfo,
                "csrf" to csrfToken.token,
            )
        } else {
            return mapOf(
                "error" to "User not authenticated",
                "csrf" to csrfToken.token,
            )
        }
    }
}