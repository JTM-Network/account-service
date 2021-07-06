package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.PasswordReset
import com.jtm.account.core.domain.exception.AccountNotFound
import com.jtm.account.core.domain.exception.PasswordResetNotFound
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.PasswordResetRepository
import com.jtm.account.core.usecase.token.TokenProvider
import com.jtm.account.data.service.MailService
import com.turbomanage.httpclient.HttpHead
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class PasswordServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val resetRepository = mock(PasswordResetRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val mailService = mock(MailService::class.java)
    private val passwordService = PasswordService(profileRepository, resetRepository, tokenProvider)

    @Test
    fun requestForgotPasswordResetTest() {
        val profile = mock(AccountProfile::class.java)
        val request = PasswordReset(token = "token", email = "email")

        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(tokenProvider.createRequestToken(anyString())).thenReturn("test")
        `when`(resetRepository.save(anyOrNull())).thenReturn(Mono.just(request))
        `when`(mailService.sendMail(anyOrNull())).thenReturn(Mono.empty())

        val returned = passwordService.requestForgotPasswordReset("test", mailService)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }

    @Test
    fun requestForgotPasswordReset_thenNotFoundTest() {
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = passwordService.requestForgotPasswordReset("test", mailService)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(AccountNotFound::class.java)
            .verify()
    }

    @Test
    fun getForgotPasswordTokenTest() {
        `when`(resetRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(PasswordReset(token = "token", email = "email")))

        val returned = passwordService.getForgotPasswordToken(UUID.randomUUID())

        verify(resetRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(resetRepository)

        StepVerifier.create(returned)
            .assertNext { assertThat(it).isEqualTo("token") }
            .verifyComplete()
    }

    @Test
    fun getForgotPasswordToken_thenNotFoundTest() {
        `when`(resetRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = passwordService.getForgotPasswordToken(UUID.randomUUID())

        verify(resetRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(resetRepository)

        StepVerifier.create(returned)
            .expectError(PasswordResetNotFound::class.java)
            .verify()
    }

    @Test
    fun resetPasswordTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)
        val profile = mock(AccountProfile::class.java)
        val encoder = mock(PasswordEncoder::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("test")
        `when`(profile.email).thenReturn("test@gmail.com")
        `when`(tokenProvider.passwordEncoder()).thenReturn(encoder)
        `when`(resetRepository.findByToken(anyString())).thenReturn(Mono.just(PasswordReset(token = "token", email = "email")))
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(profileRepository.save(anyOrNull())).thenReturn(Mono.just(profile))
        `when`(resetRepository.deleteAllByEmail(anyString())).thenReturn(Mono.empty())

        val returned = passwordService.resetPassword(request, AccountProfileDto(username = null, email = null, password = "password"))

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(resetRepository, times(1)).findByToken(anyString())
        verifyNoMoreInteractions(resetRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }
}