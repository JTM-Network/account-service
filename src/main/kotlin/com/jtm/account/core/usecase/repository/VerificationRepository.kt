package com.jtm.account.core.usecase.repository

import com.jtm.account.core.domain.entity.EmailVerification
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface VerificationRepository: ReactiveMongoRepository<EmailVerification, UUID> {
    fun findByToken(token: String): Mono<EmailVerification>

    fun findByTokenAndEmail(token: String, email: String): Mono<EmailVerification>

    fun findByEmail(email: String): Mono<EmailVerification>

    fun deleteByEmail(email: String): Mono<EmailVerification>
}