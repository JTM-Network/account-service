package com.jtm.account.data.service

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.EmailVerification
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.VerificationRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class VerifyServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val verificationRepository = mock(VerificationRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val verifyService = VerifyService(profileRepository, verificationRepository, tokenProvider)

    @Test fun requestVerificationTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "test", "test", listOf(), false)))

        val returned = verifyService.requestVerification(request)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }

    @Test fun getVerificationTest() {
        `when`(verificationRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(EmailVerification(UUID.randomUUID(), "token", System.currentTimeMillis(), System.currentTimeMillis())))

        val returned = verifyService.getVerification(UUID.randomUUID())

        verify(verificationRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(verificationRepository)

        StepVerifier.create(returned)
            .assertNext { assertThat(it).isEqualTo("token") }
            .verifyComplete()
    }

    @Test fun confirmVerificationTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(verificationRepository.findByToken(anyString())).thenReturn(Mono.just(EmailVerification(UUID.randomUUID(), "token", System.currentTimeMillis(), System.currentTimeMillis())))

        val returned = verifyService.confirmVerification(request)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(verificationRepository, times(1)).findByToken(anyString())
        verifyNoMoreInteractions(verificationRepository)

        StepVerifier.create(returned)
            .verifyComplete()
    }
}