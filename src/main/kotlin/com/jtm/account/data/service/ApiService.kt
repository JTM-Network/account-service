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
import com.jtm.account.core.util.UtilJwt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ApiService @Autowired constructor(private val tokenRepository: ApiTokenRepository,
                                        private val accountRepository: AccountProfileRepository,
                                        private val tokenProvider: TokenProvider) {

    fun createToken(request: ServerHttpRequest): Mono<ApiToken> {
        val bearer = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return Mono.error { InvalidJwtToken() }
        val token = UtilJwt.resolveToken(bearer) ?: return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token) ?: return Mono.error { InvalidJwtToken() }
        return accountRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error(AccountNotFound()) })
            .flatMap { tokenRepository.save(ApiToken(accountId = it.id, token = tokenProvider.createApiToken(it.email))) }
    }

    fun getToken(id: UUID): Mono<ApiToken> {
        return tokenRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error { ApiTokenNotFound() } })
            .flatMap {
                if (!it.valid) return@flatMap Mono.error { InvalidApiToken() }
                return@flatMap Mono.just(it)
            }
    }

    fun getAccount(request: ServerHttpRequest): Mono<AccountProfile> {
        val bearer = request.headers.getFirst("API_AUTHORIZATION") ?: return Mono.error { InvalidJwtToken() }
        val token = UtilJwt.resolveToken(bearer) ?: return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmailApi(token) ?: return Mono.error { InvalidJwtToken() }
        return accountRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error { AccountNotFound() } })
    }

    fun getTokensByAccountId(id: UUID): Flux<ApiToken> {
        return tokenRepository.findByAccountId(id)
            .filter { it.valid }
    }

    fun getTokens(request: ServerHttpRequest): Flux<ApiToken> {
        val bearer = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return Flux.error { InvalidJwtToken() }
        val token = UtilJwt.resolveToken(bearer) ?: return Flux.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token) ?: return Flux.error { InvalidJwtToken() }
        return accountRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error { AccountNotFound() } })
            .flatMapMany { tokenRepository.findByAccountId(it.id) }
            .filter { it.valid }
    }

    fun blacklistToken(request: ServerHttpRequest): Mono<ApiToken> {
        val bearer = request.headers.getFirst("API_AUTHORIZATION") ?: return Mono.error { InvalidJwtToken() }
        val token = UtilJwt.resolveToken(bearer) ?: return Mono.error { InvalidJwtToken() }
        return tokenRepository.findByToken(token)
            .switchIfEmpty(Mono.defer { Mono.error { ApiTokenNotFound() } })
            .flatMap { tokenRepository.save(it.invalidate()).thenReturn(it) }
    }
}