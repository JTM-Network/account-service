package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.RoleFound
import com.jtm.account.core.domain.exception.RoleNotFound
import com.jtm.account.core.usecase.repository.RoleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import javax.annotation.PostConstruct
import kotlin.math.log

@Service
class RoleService @Autowired constructor(private val roleRepository: RoleRepository) {

    private val logger = LoggerFactory.getLogger(RoleService::class.java)

    @PostConstruct
    fun init(): Mono<Role> {
        return roleRepository.findByPriority(0)
            .switchIfEmpty(Mono.defer { roleRepository.save(Role(name = "CLIENT", priority = 0)).doOnSuccess { logger.info("Added CLIENT role.") } })
            .then(roleRepository.findByPriority(10)
                .switchIfEmpty(Mono.defer { roleRepository.save(Role(name = "ADMIN", priority = 10)).doOnSuccess { logger.info("Added ADMIN role.") } }))
    }

    fun insertRole(role: RoleDto): Mono<Role> {
        return roleRepository.findByName(role.name)
            .flatMap<Role?> { Mono.defer { Mono.error { RoleFound() } } }.cast(Role::class.java)
            .switchIfEmpty(Mono.defer { roleRepository.save(Role(UUID.randomUUID(), role.name, role.priority)) })
    }

    fun updateRole(id: UUID, role: RoleDto): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error { RoleNotFound() } })
            .flatMap { roleRepository.save(it.update(role)) }
    }

    fun getRole(id: UUID): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error { RoleNotFound() } })
    }

    fun getRoles(): Flux<Role> {
        return roleRepository.findAll()
    }

    fun deleteRole(id: UUID): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error { RoleNotFound() } })
            .flatMap { roleRepository.delete(it).thenReturn(it) }
    }
}