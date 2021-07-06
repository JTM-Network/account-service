package com.jtm.account.data.service

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.EmailVerification
import com.jtm.account.core.domain.exception.EmailVerificationNotFound
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.VerificationRepository
import com.jtm.account.core.usecase.token.TokenProvider
import com.mailjet.client.MailjetResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
class VerifyServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val verificationRepository = mock(VerificationRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val mailService = mock(MailService::class.java)
    private val verifyService = VerifyService(profileRepository, verificationRepository, tokenProvider, mailService)
    private val verification = EmailVerification(email = "email", token = "token", createdTime = System.currentTimeMillis(), endTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))

    @Test fun requestVerificationTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)
        val response = mock(MailjetResponse::class.java)
        val emailVerification = mock(EmailVerification::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(emailVerification.isValid()).thenReturn(true)
        `when`(emailVerification.email).thenReturn("test@gmail.com")
        `when`(emailVerification.id).thenReturn(UUID.randomUUID())
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(tokenProvider.createVerificationToken(anyString())).thenReturn("token")
        `when`(verificationRepository.findByEmail(anyString())).thenReturn(Mono.empty())
        `when`(verificationRepository.save(anyOrNull())).thenReturn(Mono.just(emailVerification))
        `when`(mailService.sendMail(anyOrNull())).thenReturn(Mono.just(response))

        val returned = verifyService.requestVerification(request)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(verificationRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(verificationRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }

    @Test fun getVerificationTest() {
        `when`(verificationRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(verification))

        val returned = verifyService.getVerification(UUID.randomUUID())

        verify(verificationRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(verificationRepository)

        StepVerifier.create(returned)
            .assertNext { assertThat(it).isEqualTo("token") }
            .verifyComplete()
    }

    @Test fun confirmVerification_tokenNotFoundTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)
        val profile = mock(AccountProfile::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(verificationRepository.findByTokenAndEmail(anyString(), anyString())).thenReturn(Mono.empty())

        val returned = verifyService.confirmVerification(request)

        verify(request, times(2)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(2)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(EmailVerificationNotFound::class.java)
            .verify()
    }

    @Test fun confirmVerificationTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)
        val profile = mock(AccountProfile::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(profileRepository.save(anyOrNull())).thenReturn(Mono.just(profile))
        `when`(verificationRepository.findByTokenAndEmail(anyString(), anyString())).thenReturn(Mono.just(verification))
        `when`(verificationRepository.save(anyOrNull())).thenReturn(Mono.just(verification.confirmed()))

        val returned = verifyService.confirmVerification(request)

        verify(request, times(2)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(2)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }
}