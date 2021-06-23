package com.jtm.account.data.service

import com.jtm.account.core.domain.exception.InvalidJwtToken
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.VerificationRepository
import com.jtm.account.core.usecase.token.TokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/**
 * Steps:
 * - Request verification using an email if found it will send an email to the address
 * - Get the verification token to confirm verification from the id given in the id link
 * - Confirm the verification once the link is clicked and directing them to the https://www.jtm-network.com/verify/{id}
 */
@Service
class VerifyService @Autowired constructor(private val profileRepository: AccountProfileRepository,
                                           private val verificationRepository: VerificationRepository,
                                           private val tokenProvider: TokenProvider) {

    fun requestVerification(request: ServerHttpRequest): Mono<Void> {
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token)
        return profileRepository.findByEmail(email)
            .flatMap { Mono.empty() }
    }

    fun getVerification(id: UUID): Mono<String> {
        return verificationRepository.findById(id)
            .map { it.token }
    }

    fun confirmVerification(request: ServerHttpRequest): Mono<Void> {
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        return verificationRepository.findByToken(token)
            .flatMap { Mono.empty() }
    }
}