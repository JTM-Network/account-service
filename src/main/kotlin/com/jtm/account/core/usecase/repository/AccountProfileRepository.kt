package com.jtm.account.core.usecase.repository

import com.jtm.account.core.domain.entity.AccountProfile
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface AccountProfileRepository: ReactiveMongoRepository<AccountProfile, UUID> {

    fun findByUsername(username: String): Mono<AccountProfile>

    fun findByEmail(email: String): Mono<AccountProfile>

    fun findByUsernameOrEmail(username: String, email: String): Mono<AccountProfile>
}