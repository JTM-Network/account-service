package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.exception.account.AccountAlreadyHasRole
import com.jtm.account.core.domain.exception.account.AccountNotFound
import com.jtm.account.core.domain.exception.account.RoleNotFound
import com.jtm.account.core.domain.exception.token.CodeAlreadyUsed
import com.jtm.account.core.domain.exception.token.IncorrectAdminCode
import com.jtm.account.core.domain.exception.token.InvalidJwtToken
import com.jtm.account.core.domain.model.AuthCode
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.RoleRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

@Service
class AdminService @Autowired constructor(private val profileRepository: AccountProfileRepository,
                                          private val roleRepository: RoleRepository,
                                          private val tokenProvider: TokenProvider) {

    private val service = Executors.newScheduledThreadPool(1)
    var code: AuthCode = AuthCode()

    init {
        println("Current code: ${code.code}")
        service.schedule({
            code = AuthCode()
            println("Current admin code: ${code.code}")
                         }, 1L, TimeUnit.HOURS)
    }

    @PreDestroy
    fun destroy() {
        this.service.shutdown()
    }

    fun makeAdmin(request: ServerHttpRequest, code: String): Mono<AccountProfile> {
        if (this.code.code  != code) return Mono.error { IncorrectAdminCode() }
        if (this.code.used) return Mono.error { CodeAlreadyUsed() }
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token) ?: return Mono.error { InvalidJwtToken() }
        return profileRepository.findByEmail(email)
            .flatMap { account ->
                if (account.hasRole(10)) return@flatMap Mono.error { AccountAlreadyHasRole() }
                roleRepository.findByPriority(10)
                .switchIfEmpty(Mono.defer { Mono.error(RoleNotFound()) })
                .flatMap { profileRepository.save(account.addRole(it)) }
            }
            .map {
                this.code.used()
                it.protectedView()
            }
    }

    fun getCode(): Mono<String> {
        return Mono.just(code.code)
    }

    fun refreshCode(): Mono<Void> {
        return Mono.just(code)
                .map { this.code = AuthCode() }
                .then()
    }

    fun getAccount(id: UUID): Mono<AccountProfile> {
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.defer { Mono.error(AccountNotFound()) })
    }

    fun getAccounts(): Flux<AccountProfile> {
        return profileRepository.findAll()
    }
}