package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.AuthService
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
import reactor.core.publisher.Mono
import java.util.*

@RunWith(SpringRunner::class)
@WebFluxTest(AuthController::class)
@AutoConfigureWebTestClient
class AuthControllerTest {

    @Autowired lateinit var testClient: WebTestClient

    @MockBean lateinit var profileService: AuthService

    @Test fun registerTest() {
        `when`(profileService.register(anyOrNull())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "email", "pass", listOf(), false).protectedView()))

        testClient.post()
            .uri("/auth/register")
            .bodyValue(AccountProfileDto("username", "email", "password"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("test")
            .jsonPath("$.email").isEqualTo("email")
            .jsonPath("$.password").isEmpty
            .jsonPath("$.verified").isBoolean

        verify(profileService, times(1)).register(anyOrNull())
        verifyNoMoreInteractions(profileService)
    }

    @Test fun loginTest() {
        `when`(profileService.login(anyOrNull(), anyOrNull())).thenReturn(Mono.just("test"))

        testClient.post()
            .uri("/auth/login")
            .bodyValue(AccountProfileDto(null, "email", "Password"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEqualTo("test")

        verify(profileService, times(1)).login(anyOrNull(), anyOrNull())
        verifyNoMoreInteractions(profileService)
    }

    @Test fun meTest() {
        `when`(profileService.whoami(anyOrNull())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "test", "password", listOf(), false).protectedView()))

        testClient.get()
            .uri("/auth/me")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("test")
            .jsonPath("$.email").isEqualTo("test")
            .jsonPath("$.password").isEmpty
            .jsonPath("$.verified").isBoolean

        verify(profileService, times(1)).whoami(anyOrNull())
        verifyNoMoreInteractions(profileService)
    }

    @Test fun refreshTest() {
        `when`(profileService.refresh(anyOrNull(), anyOrNull())).thenReturn(Mono.just("test"))

        testClient.get()
            .uri("/auth/refresh")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEqualTo("test")

        verify(profileService, times(1)).refresh(anyOrNull(), anyOrNull())
        verifyNoMoreInteractions(profileService)
    }
}