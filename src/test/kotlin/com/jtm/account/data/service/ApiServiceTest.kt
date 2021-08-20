package com.jtm.account.data.service

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.ApiToken
import com.jtm.account.core.domain.exception.account.AccountNotFound
import com.jtm.account.core.domain.exception.token.ApiTokenNotFound
import com.jtm.account.core.domain.exception.token.InvalidApiToken
import com.jtm.account.core.domain.exception.token.InvalidJwtToken
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.ApiTokenRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class ApiServiceTest {

    private val tokenRepository: ApiTokenRepository = mock()
    private val tokenProvider: TokenProvider = mock()
    private val accountRepository: AccountProfileRepository = mock()
    private val apiService = ApiService(tokenRepository, accountRepository, tokenProvider)
    private val profile = AccountProfile(UUID.randomUUID(), "jty", "jty@gmail.com", "test", mutableListOf(), false)
    private val createdToken = ApiToken(token = "token", accountId = profile.id)
    private val request: ServerHttpRequest = mock()

    @Before
    fun setup() {
        val headers: HttpHeaders = mock()

        `when`(request.headers).thenReturn(headers)
        `when`(headers.getFirst(anyString())).thenReturn("Bearer test")
    }

    @Test
    fun createToken_thenInvalidEmail() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn(null)

        val returned = apiService.createToken(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        StepVerifier.create(returned)
            .expectError(InvalidJwtToken::class.java)
            .verify()
    }

    @Test
    fun createToken_thenAccountNotFound() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = apiService.createToken(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .expectError(AccountNotFound::class.java)
            .verify()
    }

    @Test
    fun createTokenTest() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(tokenProvider.createApiToken(anyString())).thenReturn("token")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(tokenRepository.save(anyOrNull())).thenReturn(Mono.just(createdToken))

        val returned = apiService.createToken(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.valid).isTrue
                assertThat(it.token).isEqualTo("token")
                assertThat(it.accountId).isEqualTo(profile.id)
            }
            .verifyComplete()
    }

    @Test
    fun getToken_thenNotFound() {
        `when`(tokenRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = apiService.getToken(UUID.randomUUID())

        verify(tokenRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .expectError(ApiTokenNotFound::class.java)
            .verify()
    }

    @Test
    fun getToken_thenInvalidApiToken() {
        `when`(tokenRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(createdToken.invalidate()))

        val returned = apiService.getToken(UUID.randomUUID())

        verify(tokenRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .expectError(InvalidApiToken::class.java)
            .verify()
    }

    @Test
    fun getTokenTest() {
        `when`(tokenRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(createdToken))

        val returned = apiService.getToken(UUID.randomUUID())

        verify(tokenRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.token).isEqualTo("token")
            }
            .verifyComplete()
    }

    @Test
    fun getAccount_thenEmailInvalid() {
        `when`(tokenProvider.getEmailApi(anyString())).thenReturn(null)

        val returned = apiService.getAccount(request)

        verify(tokenProvider, times(1)).getEmailApi(anyString())
        verifyNoMoreInteractions(tokenProvider)

        StepVerifier.create(returned)
            .expectError(InvalidJwtToken::class.java)
            .verify()
    }

    @Test
    fun getAccount_thenAccountNotFound() {
        `when`(tokenProvider.getEmailApi(anyString())).thenReturn("test@gmail.com")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = apiService.getAccount(request)

        verify(tokenProvider, times(1)).getEmailApi(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .expectError(AccountNotFound::class.java)
            .verify()
    }

    @Test
    fun getAccountTest() {
        `when`(tokenProvider.getEmailApi(anyString())).thenReturn("test@gmail.com")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))

        val returned = apiService.getAccount(request)

        verify(tokenProvider, times(1)).getEmailApi(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.id).isEqualTo(profile.id)
                assertThat(it.email).isEqualTo(profile.email)
            }
            .verifyComplete()
    }

    @Test
    fun getTokensByAccountIdTest() {
        `when`(tokenRepository.findByAccountId(anyOrNull())).thenReturn(Flux.just(createdToken))

        val returned = apiService.getTokensByAccountId(UUID.randomUUID())

        verify(tokenRepository, times(1)).findByAccountId(anyOrNull())
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.token).isEqualTo("token")
                assertThat(it.accountId).isEqualTo(profile.id)
            }
            .verifyComplete()
    }

    @Test
    fun getTokens_thenEmailInvalid() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn(null)

        val returned = apiService.getTokens(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        StepVerifier.create(returned)
            .expectError(InvalidJwtToken::class.java)
            .verify()
    }

    @Test
    fun getTokens_thenAccountNotFound() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.empty())

        val returned = apiService.getTokens(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .expectError(AccountNotFound::class.java)
            .verify()
    }

    @Test
    fun getTokensTest() {
        `when`(tokenProvider.getEmail(anyString())).thenReturn("test")
        `when`(accountRepository.findByEmail(anyString())).thenReturn(Mono.just(profile))
        `when`(tokenRepository.findByAccountId(anyOrNull())).thenReturn(Flux.just(createdToken))

        val returned = apiService.getTokens(request)

        verify(tokenProvider, times(1)).getEmail(anyString())
        verifyNoMoreInteractions(tokenProvider)

        verify(accountRepository, times(1)).findByEmail(anyString())
        verifyNoMoreInteractions(accountRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.token).isEqualTo("token")
                assertThat(it.accountId).isEqualTo(profile.id)
            }
            .verifyComplete()
    }

    @Test
    fun blacklistToken_thenTokenNotFound() {
        `when`(tokenRepository.findByToken(anyString())).thenReturn(Mono.empty())

        val returned = apiService.blacklistToken(request)

        verify(tokenRepository, times(1)).findByToken(anyString())
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .expectError(ApiTokenNotFound::class.java)
            .verify()
    }

    @Test
    fun blacklistTokenTest() {
        `when`(tokenRepository.findByToken(anyString())).thenReturn(Mono.just(createdToken))
        `when`(tokenRepository.save(anyOrNull())).thenReturn(Mono.just(createdToken.invalidate()))

        val returned = apiService.blacklistToken(request)

        verify(tokenRepository, times(1)).findByToken(anyString())
        verifyNoMoreInteractions(tokenRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.token).isEqualTo("token")
                assertThat(it.accountId).isEqualTo(profile.id)
            }
            .verifyComplete()
    }
}