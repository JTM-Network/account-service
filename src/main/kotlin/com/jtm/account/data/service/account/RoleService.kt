package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.account.RoleFound
import com.jtm.account.core.domain.exception.account.RoleNotFound
import com.jtm.account.core.usecase.repository.RoleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Exception
import java.util.*
import javax.annotation.PostConstruct

@Service
class RoleService @Autowired constructor(private val roleRepository: RoleRepository) {

    private val logger = LoggerFactory.getLogger(RoleService::class.java)

    @PostConstruct
    fun init() {
        try {
            val updated = getRoleByName("CLIENT").block() ?: return
            updateRole(updated.id, RoleDto(updated.name, 0)).block()
        } catch (ex: Exception) {
            logger.error("Failed to update client: ${ex.message}")
        }
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

    fun getRoleByName(name: String): Mono<Role> {
        return roleRepository.findByName(name)
    }

    fun getRoles(): Flux<Role> {
        return roleRepository.findAll()
    }

    fun deleteRole(id: UUID): Mono<Role> {
        return roleRepository.findById(id)
            .switchIfEmpty(Mono.defer { Mono.error { RoleNotFound() } })
            .flatMap { roleRepository.delete(it).thenReturn(it) }
    }

    fun deleteAll(): Mono<Void> {
        return roleRepository.deleteAll()
    }
}