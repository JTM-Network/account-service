package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.PasswordReset
import com.jtm.account.core.domain.exception.*
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import com.jtm.account.core.usecase.repository.PasswordResetRepository
import com.jtm.account.core.usecase.token.TokenProvider
import com.jtm.account.core.util.MailjetRequestBuilder
import com.jtm.account.data.service.MailService
import com.mailjet.client.MailjetRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class PasswordService @Autowired constructor(
    private val profileRepository: AccountProfileRepository,
    private val resetRepository: PasswordResetRepository,
    private val tokenProvider: TokenProvider) {

    fun requestForgotPasswordReset(email: String, mailService: MailService): Mono<Void> {
        return profileRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer { Mono.error(AccountNotFound()) })
            .flatMap {
                resetRepository.save(PasswordReset(token = tokenProvider.createRequestToken(email), email = email))
                    .flatMap { mailService.sendMail(resetEmail(it.id, email)).then() }
            }
    }

    fun getForgotPasswordToken(id: UUID): Mono<String> {
        return resetRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error(PasswordResetNotFound()) })
            .map { it.token }
    }

    fun resetPassword(request: ServerHttpRequest, profileDto: AccountProfileDto): Mono<Void> {
        val token = request.headers.getFirst("Request") ?: return Mono.error { InvalidResetToken() }
        val password = profileDto.password ?: return Mono.error { InvalidPassword() }
        return resetRepository.findByToken(token)
            .flatMap {
                val email = tokenProvider.getEmailPasswordReset(it.token)
                return@flatMap profileRepository.findByEmail(email)
                    .flatMap { profile ->
                        profileRepository.save(profile.setPassword(password, tokenProvider.passwordEncoder()))
                            .flatMap { resetRepository.deleteAllByEmail(profile.email) }
                    }
            }
    }

    private fun resetEmail(id: UUID, email: String): MailjetRequest {
        return MailjetRequestBuilder()
            .withFrom("no-reply@jtm-network.com", "JTM Network")
            .withTo(email)
            .withSubject("Password Reset")
            .withText("Click this link to reset your password on your account: https://www.jtm-network.com/reset/${id.toString()}")
            .build()
    }
}