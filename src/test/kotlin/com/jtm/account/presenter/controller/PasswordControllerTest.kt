package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.data.service.MailService
import com.jtm.account.data.service.account.PasswordService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
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
@WebFluxTest(PasswordController::class)
@AutoConfigureWebTestClient
class PasswordControllerTest {

    @Autowired
    lateinit var testClient: WebTestClient

    @MockBean
    lateinit var passwordService: PasswordService

    @MockBean
    lateinit var mailService: MailService

    @Test
    fun forgotPasswordResetTest() {
        `when`(passwordService.requestForgotPasswordReset(anyString(), anyOrNull())).thenReturn(Mono.empty())

        testClient.get()
            .uri("/forgot-password/request?email=test@gmail.com")
            .exchange()
            .expectStatus().isOk

        verify(passwordService, times(1)).requestForgotPasswordReset(anyString(), anyOrNull())
        verifyNoMoreInteractions(passwordService)
    }

    @Test
    fun getRequestTest() {
        `when`(passwordService.getForgotPasswordToken(anyOrNull())).thenReturn(Mono.just("test"))

        testClient.get()
            .uri("/forgot-password/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEqualTo("test")

        verify(passwordService, times(1)).getForgotPasswordToken(anyOrNull())
        verifyNoMoreInteractions(passwordService)
    }

    @Test
    fun resetPasswordTest() {
        `when`(passwordService.resetPassword(anyOrNull(), anyOrNull())).thenReturn(Mono.empty())

        testClient.post()
            .uri("/forgot-password/reset")
            .bodyValue(AccountProfileDto(username = null, email = null, password = "password"))
            .exchange()
            .expectStatus().isOk

        verify(passwordService, times(1)).resetPassword(anyOrNull(), anyOrNull())
        verifyNoMoreInteractions(passwordService)
    }
}