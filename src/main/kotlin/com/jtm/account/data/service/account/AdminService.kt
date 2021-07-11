package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.exception.AccountAlreadyHasRole
import com.jtm.account.core.domain.exception.IncorrectAdminCode
import com.jtm.account.core.domain.exception.InvalidJwtToken
import com.jtm.account.core.domain.exception.RoleNotFound
import com.jtm.account.core.domain.model.AuthCode
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.RoleRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
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
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token)
        return profileRepository.findByEmail(email)
            .flatMap { account ->
                if (account.hasRole(10)) return@flatMap Mono.error { AccountAlreadyHasRole() }
                roleRepository.findByPriority(10)
                .switchIfEmpty(Mono.defer { Mono.error(RoleNotFound()) })
                .flatMap { profileRepository.save(account.addRole(it)) }
            }
    }
}