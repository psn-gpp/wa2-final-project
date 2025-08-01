package it.wa2.reservationservice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.WebClient
/*
@RestController
@RequestMapping("/api/users")
class ProxyController(builder: WebClient.Builder) {

    val client: WebClient = builder.baseUrl("http://localhost:8081").build()

    @RequestMapping("/**", method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE])
    fun proxyRequest(
        request: HttpServletRequest,
        entity: HttpEntity<String>
    ): ResponseEntity<String> {
        val path = request.requestURI.removePrefix("/api/users")
        val query = request.queryString?.let { "?$it" } ?: ""
        val fullUri = path + java.net.URLDecoder.decode(query, "UTF-8")

        println("Proxying to: $fullUri")

        val method = try {
            HttpMethod.valueOf(request.method.uppercase())
        } catch (e: IllegalArgumentException) {
            HttpMethod.GET
        }

        val proxiedResponse = client.method(method)
            .uri(fullUri)
            .headers { headers -> headers.addAll(entity.headers) }
            .bodyValue(entity.body ?: "")
            .retrieve()
            .toEntity(String::class.java)
            .block()

        return ResponseEntity
            .status(proxiedResponse?.statusCode ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .headers(proxiedResponse?.headers ?: HttpHeaders())
            .body(proxiedResponse?.body)
    }
}
 */