package it.wa2.reservationservice

import org.bouncycastle.util.Integers
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class TokenState{
    VALID, IN_EXPIRATION, IN_REQUEST, NONE
}

@Component
class ManageToken {
    @Value("\${spring.security.oauth2.client.registration.keycloak.client-id}")
    lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.keycloak.client-secret}")
    lateinit var clientSecret: String

    @Value("\${spring.security.oauth2.client.provider.keycloak.token-uri}")
    lateinit var tokenUri: String

    private var token: String = ""
    private var expiresIn: LocalDateTime = LocalDateTime.MIN
    private var state: TokenState = TokenState.NONE

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private fun getAccessToken(): String {
        lock.withLock {
            state = TokenState.IN_REQUEST

            try {
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                }
                val body = LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", "client_credentials")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                }
                val request = HttpEntity(body, headers)
                //println("sono qui token: $tokenUri")
                val response = RestTemplate().postForEntity(tokenUri, request, Map::class.java)
                //println("tutta la richiesta $response")
                expiresIn = LocalDateTime.now().plusSeconds((response.body?.get("expires_in") as? Int)?.toLong() ?: 0L)
                token = response.body?.get("access_token") as String
                state = TokenState.VALID
                //println("token rinnovato")
                condition.signalAll()
                return response.body?.get("access_token") as String
            }catch (e: Exception) {
                //println("eccezione "+e.message)
                state = TokenState.NONE
                condition.signalAll()
                throw e
            }
        }

    }

    fun getCachedToken(): String {
        lock.withLock {
            if (Duration.between(LocalDateTime.now(), expiresIn) < Duration.ofSeconds(60)) {
                //println("token in scadenza")
                state = TokenState.IN_EXPIRATION
            }

            if(state == TokenState.NONE || state == TokenState.IN_EXPIRATION) {
                //println("chiedo nuovo token")
                return getAccessToken()
            }

            while(state==TokenState.IN_REQUEST){
                //println("qualcuno sta chiedendo nuovo token")
                condition.await()
            }

            //state==TokenState.VALID
            //println("token valido")
            return token
        }
    }
}