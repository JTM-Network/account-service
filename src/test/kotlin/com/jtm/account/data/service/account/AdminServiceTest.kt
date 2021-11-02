package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.account.AccountAlreadyHasRole
import com.jtm.account.core.domain.exception.account.AccountNotFound
import com.jtm.account.core.domain.exception.account.RoleNotFound
import com.jtm.account.core.domain.exception.token.CodeAlreadyUsed
import com.jtm.account.core.domain.exception.token.IncorrectAdminCode
import com.jtm.account.core.domain.exception.token.InvalidJwtToken
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.RoleRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class AdminServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val roleRepository = mock(RoleRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val adminService = AdminService(profileRepository, roleRepository, tokenProvider)
    private val request = mock(ServerHttpRequest::class.java)
    private val headers = mock(HttpHeaders::class.java)
    private val account = AccountProfile(UUID.randomUUID(), "test", "email", "", mutableListOf(), false)

    @Test
    fun makeAdminTest() {
        val role = mock(Role::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(account))
        `when`(roleRepository.findByPriority(anyInt())).thenReturn(Mono.just(role))
        `when`(profileRepository.save(anyOrNull())).thenReturn(Mono.just(account))

        val returned = adminService.makeAdmin(request, adminService.code.code)

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
                assertThat(it.email).isEqualTo("email")
                assertThat(it.verified).isFalse()
            }
            .verifyComplete()
    }

    @Test
    fun makeAdmin_thenIncorrectCodeTest() {
        val returned = adminService.makeAdmin(request, "test")

        StepVerifier.create(returned)
            .expectError(IncorrectAdminCode::class.java)
            .verify()
    }

    @Test
    fun makeAdmin_thenCodeAlreadyUsedTest() {
        adminService.code.used()

        val returned = adminService.makeAdmin(request, adminService.code.code)

        StepVerifier.create(returned)
            .expectError(CodeAlreadyUsed::class.java)
            .verify()
    }

    @Test
    fun makeAdmin_thenNoAuthHeaderTest() {
        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn(null)

        val returned = adminService.makeAdmin(request, adminService.code.code)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        StepVerifier.create(returned)
            .expectError(InvalidJwtToken::class.java)
            .verify()
    }

    @Test
    fun makeAdmin_thenAccountAlreadyHasRoleTest() {
        val accountProfile = mock(AccountProfile::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(accountProfile))
        `when`(accountProfile.hasRole(anyInt())).thenReturn(true)

        val returned = adminService.makeAdmin(request, adminService.code.code)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(AccountAlreadyHasRole::class.java)
            .verify()
    }

    @Test
    fun makeAdmin_thenRoleNotFoundTest() {
        val accountProfile = mock(AccountProfile::class.java)

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(profileRepository.findByEmail(anyString())).thenReturn(Mono.just(accountProfile))
        `when`(accountProfile.hasRole(anyInt())).thenReturn(false)
        `when`(roleRepository.findByPriority(anyInt())).thenReturn(Mono.empty())

        val returned = adminService.makeAdmin(request, adminService.code.code)

        verify(request, times(1)).headers
        verifyNoMoreInteractions(request)

        verify(headers, times(1)).getFirst(anyString())
        verifyNoMoreInteractions(headers)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(profileRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
            .expectError(RoleNotFound::class.java)
            .verify()
    }

    @Test
    fun getAccount_thenNotFound() {
        `when`(profileRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = adminService.getAccount(UUID.randomUUID())

        verify(profileRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .expectError(AccountNotFound::class.java)
                .verify()
    }

    @Test
    fun getAccount() {
        `when`(profileRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(account))

        val returned = adminService.getAccount(UUID.randomUUID())

        verify(profileRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .assertNext {
                    assertThat(it.id).isEqualTo(account.id)
                    assertThat(it.username).isEqualTo(account.username)
                    assertThat(it.email).isEqualTo(account.email)
                }
                .verifyComplete()
    }

    @Test
    fun getAccounts() {
        `when`(profileRepository.findAll()).thenReturn(Flux.just(account))

        val returned = adminService.getAccounts()

        verify(profileRepository, times(1)).findAll()
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .assertNext {
                    assertThat(it.id).isEqualTo(account.id)
                    assertThat(it.username).isEqualTo(account.username)
                    assertThat(it.email).isEqualTo(account.email)
                }
    }
}