package com.jtm.account.data.service

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.AccountNotFound
import com.jtm.account.core.domain.exception.EmailOrUsernameFound
import com.jtm.account.core.domain.exception.InvalidEmailOrPass
import com.jtm.account.core.domain.exception.RoleNotFound
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.RoleRepository
import com.jtm.account.core.usecase.token.TokenProvider
import com.turbomanage.httpclient.HttpHead
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import kotlin.collections.ArrayList

@RunWith(SpringRunner::class)
class AuthServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val roleRepository = mock(RoleRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val profileService = AuthService(profileRepository, roleRepository, tokenProvider)
    private val accountProfile = mock(AccountProfile::class.java)

    @Test fun registerTest() {
        val encoder = mock(PasswordEncoder::class.java)

        `when`(profileRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Mono.empty())
        `when`(roleRepository.findByPriority(anyInt())).thenReturn(Mono.just(Role(UUID.randomUUID(), "CLIENT", 0)))
        `when`(profileRepository.save(anyOrNull())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "test", "test", ArrayList(), false)))
        `when`(tokenProvider.passwordEncoder()).thenReturn(encoder)
        `when`(encoder.encode(anyString())).thenReturn("test")

        val returned = profileService.register(AccountProfileDto("username", "email", "password"))

        verify(profileRepository, times(1)).findByUsernameOrEmail(anyString(), anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.username).isEqualTo("test")
                assertThat(it.email).isEqualTo("test")
                assertThat(it.verified).isFalse()
            }
            .verifyComplete()
    }

    @Test fun register_thenEmailOrUsernameFoundTest() {
        `when`(profileRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Mono.just(mock(AccountProfile::class.java)))

        val returned = profileService.register(AccountProfileDto("username", "email", "password"))

        verify(profileRepository, times(1)).findByUsernameOrEmail(anyString(), anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(EmailOrUsernameFound::class.java)
            .verify()
    }

    @Test fun register_thenRoleNotFoundTest() {
        `when`(profileRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Mono.empty())
        `when`(roleRepository.findByPriority(anyInt())).thenReturn(Mono.empty())

        val returned = profileService.register(AccountProfileDto("username", "email", "password"))

        verify(profileRepository, times(1)).findByUsernameOrEmail(anyString(), anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(RoleNotFound::class.java)
            .verify()
    }

    @Test fun loginTest() {
        val httpResponse = mock(ServerHttpResponse::class.java)
        val headers = mock(HttpHeaders::class.java)

        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(accountProfile))
        `when`(tokenProvider.passwordEncoder()).thenReturn(mock(PasswordEncoder::class.java))
        `when`(tokenProvider.createAccessToken(anyOrNull())).thenReturn("access")
        `when`(tokenProvider.createRefreshToken(anyOrNull())).thenReturn("refresh")
        `when`(tokenProvider.createAccessCookieToken(anyOrNull())).thenReturn("cookie")
        `when`(accountProfile.passwordMatches(anyString(), anyOrNull())).thenReturn(true)
        `when`(httpResponse.headers).thenReturn(headers)

        val returned = profileService.login(AccountProfileDto(null, "email", "password"), httpResponse)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it).isEqualTo("access")
            }
            .verifyComplete()
    }

    @Test fun login_thenAccountNotFoundTest() {
        val httpResponse = mock(ServerHttpResponse::class.java)

        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = profileService.login(AccountProfileDto(null, "email", "password"), httpResponse)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(InvalidEmailOrPass::class.java)
            .verify()
    }

    @Test fun whoamiTest() {
        val request = mock(ServerHttpRequest::class.java)

        val headers = mock(HttpHeaders::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "test", "password", listOf(), false)))

        val returned = profileService.whoami(request)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.username).isEqualTo("test")
                assertThat(it.email).isEqualTo("test")
                assertThat(it.verified).isFalse()
            }
            .verifyComplete()
    }

    @Test fun whoami_thenAccountNotFoundTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = profileService.whoami(request)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(AccountNotFound::class.java)
            .verify()
    }

    @Test fun refreshTest() {
        val request = mock(ServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val headers = mock(HttpHeaders::class.java)
        val cookies: MultiValueMap<String, HttpCookie> = mock()
        val cookie = mock(HttpCookie::class.java)

        `when`(response.headers).thenReturn(headers)
        `when`(request.cookies).thenReturn(cookies)
        `when`(cookies.getFirst("refreshToken")).thenReturn(cookie)
        `when`(cookie.value).thenReturn("test")
        `when`(tokenProvider.getEmailRefresh(anyString())).thenReturn("test")
        `when`(tokenProvider.createAccessToken(anyOrNull())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(accountProfile))

        val returned = profileService.refresh(request, response)

        verify(request, times(1)).cookies
        verifyNoMoreInteractions(request)

        verify(cookies, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(cookies)

        verify(cookie, times(1)).value
        verifyNoMoreInteractions(cookie)

        verify(tokenProvider, times(1)).getEmailRefresh(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .assertNext { assertThat(it).isEqualTo("test") }
            .verifyComplete()
    }
}