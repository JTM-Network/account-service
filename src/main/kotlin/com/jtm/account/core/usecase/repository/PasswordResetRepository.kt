package com.jtm.account.core.usecase.repository

import com.jtm.account.core.domain.entity.PasswordReset
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface PasswordResetRepository: ReactiveMongoRepository<PasswordReset, UUID> {

    fun findByToken(token: String): Mono<PasswordReset>

    fun deleteAllByEmail(email: String): Mono<Void>
}