package com.jtm.account.core.usecase.repository

import com.jtm.account.core.domain.entity.Role
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface RoleRepository: ReactiveMongoRepository<Role, UUID> {

    fun findByName(name: String): Mono<Role>
}