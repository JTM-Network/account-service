package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.exception.*
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.RoleRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class AuthService @Autowired constructor(private val profileRepository: AccountProfileRepository,
                                         private val roleRepository: RoleRepository,
                                         private val tokenProvider: TokenProvider) {

    fun register(accountProfileDto: AccountProfileDto): Mono<AccountProfile> {
        val username = accountProfileDto.username ?: return Mono.error { InvalidCredentials() }
        val email = accountProfileDto.email ?: return Mono.error { InvalidCredentials() }
        val password = accountProfileDto.password ?: return Mono.error { InvalidCredentials() }

        return profileRepository.findByUsernameOrEmail(username, email)
            .flatMap<AccountProfile?> { Mono.defer { Mono.error { EmailOrUsernameFound() } } }.cast(AccountProfile::class.java)
            .switchIfEmpty(Mono.defer {
                roleRepository.findByPriority(0)
                    .switchIfEmpty(Mono.defer { Mono.error { RoleNotFound() } })
                    .flatMap { role ->
                        profileRepository.save(
                            AccountProfile(
                                UUID.randomUUID(),
                                username,
                                email,
                                "",
                                mutableListOf(role),
                                false).setPassword(password, tokenProvider.passwordEncoder())
                        ).map { it.protectedView() }
                    }
            })
    }

    fun login(profileDto: AccountProfileDto, response: ServerHttpResponse): Mono<String> {
        val email = profileDto.email ?: return Mono.error { InvalidEmailOrPass() }
        val password = profileDto.password ?: return Mono.error { InvalidEmailOrPass() }
        return profileRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error { InvalidEmailOrPass() } })
            .flatMap {
                if (!it.passwordMatches(password, tokenProvider.passwordEncoder())) return@flatMap Mono.error { InvalidEmailOrPass() }
                response.headers.add("Set-Cookie", "refreshToken=${tokenProvider.createRefreshToken(it)};Max-Age=5184000000;SameSite=None; HttpOnly; Path=/; Secure")
                response.headers.add("Set-Cookie", "accessToken=${tokenProvider.createAccessCookieToken(it)};Max-Age=600000;SameSite=None; HttpOnly; Path=/; Secure")
                return@flatMap Mono.just(tokenProvider.createAccessToken(it))
            }
    }

    fun whoami(request: ServerHttpRequest): Mono<AccountProfile> {
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token =  if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token)
        return profileRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error { AccountNotFound() } })
            .map { it.protectedView() }
    }

    fun refresh(request: ServerHttpRequest, response: ServerHttpResponse): Mono<String> {
        val cookie = request.cookies.getFirst("refreshToken") ?: return Mono.error { InvalidJwtToken() }
        if (cookie.value.isEmpty()) return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmailRefresh(cookie.value)
        return profileRepository.findByEmail(email)
            .flatMap {
                response.headers.add("Set-Cookie", "accessToken=${tokenProvider.createAccessCookieToken(it)};Max-Age=600000;SameSite=None; HttpOnly; Path=/; Secure")
                return@flatMap Mono.just(tokenProvider.createAccessToken(it))
            }
    }

    fun logout(response: ServerHttpResponse): Mono<Void> {
        val headers = response.headers
        headers.add("Set-Cookie", "refreshToken=;SameSite=None; HttpOnly; Path=/; Secure")
        headers.add("Set-Cookie", "accessToken=;SameSite=None; HttpOnly; Path=/; Secure")
        return Mono.empty()
    }
}