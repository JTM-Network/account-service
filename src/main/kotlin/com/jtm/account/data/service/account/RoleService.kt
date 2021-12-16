package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.account.RoleFound
import com.jtm.account.core.domain.exception.account.RoleNotFound
import com.jtm.account.core.usecase.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import javax.annotation.PostConstruct

@Service
class RoleService @Autowired constructor(private val roleRepository: RoleRepository) {

    @PostConstruct
    fun init() {
        insertRole(RoleDto("ADMIN", 10)).block()
        insertRole(RoleDto("CLIENT", 0)).block()

        getRole("CLIENT")
                .flatMap { updateRole(it.id, RoleDto(it.name, 0)) }
                .block()
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

    fun getRole(name: String): Mono<Role> {
        return roleRepository.findByName(name)
                .switchIfEmpty(Mono.defer { Mono.error(RoleNotFound()) })
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