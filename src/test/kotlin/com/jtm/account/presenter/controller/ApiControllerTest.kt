package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.ApiToken
import com.jtm.account.data.service.ApiService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RunWith(SpringRunner::class)
@WebFluxTest(ApiController::class)
@AutoConfigureWebTestClient
class ApiControllerTest {

    @Autowired lateinit var testClient: WebTestClient
    @MockBean lateinit var apiService: ApiService
    private val createdToken = ApiToken(token = "token", accountId = UUID.randomUUID())
    private val createAccount = AccountProfile(UUID.randomUUID(), "jty", "jty@gmail.com", "test", mutableListOf(), false)

    @Test
    fun createTokenTest() {
        `when`(apiService.createToken(anyOrNull())).thenReturn(Mono.just(createdToken))

        testClient.post()
            .uri("/api-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isEqualTo("token")
            .jsonPath("$.accountId").isEqualTo(createdToken.accountId.toString())
            .jsonPath("$.valid").isBoolean

        verify(apiService, times(1)).createToken(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }

    @Test
    fun getTokenTest() {
        `when`(apiService.getToken(anyOrNull())).thenReturn(Mono.just(createdToken))

        testClient.get()
            .uri("/api-token/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isEqualTo("token")
            .jsonPath("$.accountId").isEqualTo(createdToken.accountId.toString())
            .jsonPath("$.valid").isBoolean

        verify(apiService, times(1)).getToken(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }

    @Test
    fun getAccountTest() {
        `when`(apiService.getAccount(anyOrNull())).thenReturn(Mono.just(createAccount))

        testClient.get()
            .uri("/api-token/account")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("jty")
            .jsonPath("$.email").isEqualTo("jty@gmail.com")

        verify(apiService, times(1)).getAccount(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }

    @Test
    fun getTokensByAccountIdTest() {
        `when`(apiService.getTokensByAccountId(anyOrNull())).thenReturn(Flux.just(createdToken))

        testClient.get()
            .uri("/api-token/tokens/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].token").isEqualTo("token")
            .jsonPath("$[0].accountId").isEqualTo(createdToken.accountId.toString())
            .jsonPath("$[0].valid").isBoolean

        verify(apiService, times(1)).getTokensByAccountId(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }

    @Test
    fun getTokensTest() {
        `when`(apiService.getTokens(anyOrNull())).thenReturn(Flux.just(createdToken))

        testClient.get()
            .uri("/api-token/tokens")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].token").isEqualTo("token")
            .jsonPath("$[0].accountId").isEqualTo(createdToken.accountId.toString())
            .jsonPath("$[0].valid").isBoolean

        verify(apiService, times(1)).getTokens(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }

    @Test
    fun blacklistTokenTest() {
        `when`(apiService.blacklistToken(anyOrNull())).thenReturn(Mono.just(createdToken))

        testClient.delete()
            .uri("/api-token/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").isEqualTo("token")
            .jsonPath("$.accountId").isEqualTo(createdToken.accountId.toString())
            .jsonPath("$.valid").isBoolean


        verify(apiService, times(1)).blacklistToken(anyOrNull())
        verifyNoMoreInteractions(apiService)
    }
}