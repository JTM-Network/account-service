package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.account.AccountService
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
@WebFluxTest(AccountController::class)
@AutoConfigureWebTestClient
class AccountControllerTest {

    @Autowired
    private lateinit var testClient: WebTestClient

    @MockBean
    private lateinit var accountService: AccountService

    private val profile = AccountProfile(UUID.randomUUID(), "test", "test@gmail.com", "pass", mutableListOf(), false)

    @Test
    fun getAccount() {
        `when`(accountService.getAccount(anyOrNull())).thenReturn(Mono.just(profile))

        testClient.get()
                .uri("/account/${UUID.randomUUID()}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(profile.id.toString())
                .jsonPath("$.username").isEqualTo(profile.username)
                .jsonPath("$.email").isEqualTo(profile.email)

        verify(accountService, times(1)).getAccount(anyOrNull())
        verifyNoMoreInteractions(accountService)
    }

    @Test
    fun getAccounts() {
        `when`(accountService.getAccounts()).thenReturn(Flux.just(profile))

        testClient.get()
                .uri("/account/all")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(profile.id.toString())
                .jsonPath("$[0].username").isEqualTo(profile.username)
                .jsonPath("$[0].email").isEqualTo(profile.email)

        verify(accountService, times(1)).getAccounts()
        verifyNoMoreInteractions(accountService)
    }
}