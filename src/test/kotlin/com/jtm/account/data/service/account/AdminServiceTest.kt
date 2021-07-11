package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.Role
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
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class AdminServiceTest {

    private val profileRepository = mock(AccountProfileRepository::class.java)
    private val roleRepository = mock(RoleRepository::class.java)
    private val tokenProvider = mock(TokenProvider::class.java)
    private val adminService = AdminService(profileRepository, roleRepository, tokenProvider)

    @Test
    fun makeAdminTest() {
        val request = mock(ServerHttpRequest::class.java)
        val headers = mock(HttpHeaders::class.java)
        val account = AccountProfile(UUID.randomUUID(), "test", "email", "", mutableListOf(), false)
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
}