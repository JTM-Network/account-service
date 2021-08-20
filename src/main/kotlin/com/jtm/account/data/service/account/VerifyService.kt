package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.EmailVerification
import com.jtm.account.core.domain.exception.account.EmailVerificationNotFound
import com.jtm.account.core.domain.exception.token.InvalidJwtToken
import com.jtm.account.core.domain.exception.account.InvalidVerifyToken
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.VerificationRepository
import com.jtm.account.core.usecase.token.TokenProvider
import com.jtm.account.core.util.MailjetRequestBuilder
import com.jtm.account.data.service.MailService
import com.mailjet.client.MailjetRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Steps:
 * - Request verification using an email if found it will send an email to the address
 * - Get the verification token to confirm verification from the id given in the id link
 * - Confirm the verification once the link is clicked and directing them to the https://www.jtm-network.com/verify/{id}
 */
@Service
class VerifyService @Autowired constructor(private val profileRepository: AccountProfileRepository,
                                           private val verificationRepository: VerificationRepository,
                                           private val tokenProvider: TokenProvider,
                                           private val mailService: MailService) {

    fun requestVerification(request: ServerHttpRequest): Mono<Void> {
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val email = tokenProvider.getEmail(token) ?: return Mono.error { InvalidJwtToken() }
        return verificationRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { verificationRepository.save(EmailVerification(email = email, token = tokenProvider.createVerificationToken(email), createdTime = System.currentTimeMillis(), endTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))) })
            .flatMap {
                if (!it.isValid()) return@flatMap verificationRepository.deleteByEmail(email).flatMap { verificationRepository.save(EmailVerification(email = email, token = tokenProvider.createVerificationToken(email), createdTime = System.currentTimeMillis(), endTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))) }
                    else return@flatMap Mono.just(it)
            }
            .flatMap { mailService.sendMail(verificationEmail(it.id, it.email)).then() }
    }

    fun getVerification(id: UUID): Mono<String> {
        return verificationRepository.findById(id)
            .map { it.token }
    }

    fun confirmVerification(request: ServerHttpRequest): Mono<Void> {
        val bearer = request.headers.getFirst("Authorization") ?: return Mono.error { InvalidJwtToken() }
        val token = if (bearer.startsWith("Bearer ")) bearer.replace("Bearer ", "") else return Mono.error { InvalidJwtToken() }
        val verify = request.headers.getFirst("Verify") ?: return Mono.error { InvalidVerifyToken() }
        val email = tokenProvider.getEmail(token) ?: return Mono.error { InvalidJwtToken() }
        return profileRepository.findByEmail(email)
            .flatMap { profile ->
                return@flatMap verificationRepository.findByTokenAndEmail(verify, email)
                    .switchIfEmpty(Mono.defer { Mono.error(EmailVerificationNotFound()) })
                    .flatMap { verificationRepository.save(it.confirmed())
                            .flatMap { profileRepository.save(profile.verified()).then() }
                    }
            }
    }

    private fun verificationEmail(id: UUID, email: String): MailjetRequest {
        return MailjetRequestBuilder()
            .withFrom("no-reply@jtm-network.com", "JTM Network")
            .withTo(email)
            .withSubject("Email Verification")
            .withText("Click this link to verify your account: https://www.jtm-network.com/verify/${id.toString()}")
            .build()
    }
}