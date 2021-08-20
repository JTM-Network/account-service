package com.jtm.account.core.usecase.repository

import com.jtm.account.core.domain.entity.ApiToken
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface ApiTokenRepository: ReactiveMongoRepository<ApiToken, UUID> {

    fun findByAccountId(accountId: UUID): Flux<ApiToken>

    fun findByToken(token: String): Mono<ApiToken>
}