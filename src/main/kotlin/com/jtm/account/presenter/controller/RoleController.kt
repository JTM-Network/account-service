package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.data.service.RoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/role")
class RoleController @Autowired constructor(private val roleService: RoleService) {

    @PostMapping
    fun postRole(@RequestBody body: RoleDto): Mono<Role> = roleService.insertRole(body)

    @PutMapping("/{id}")
    fun putRole(@PathVariable id: UUID, @RequestBody body: RoleDto): Mono<Role> = roleService.updateRole(id, body)

    @GetMapping("/{id}")
    fun getRole(@PathVariable id: UUID): Mono<Role> = roleService.getRole(id)

    @GetMapping("/all")
    fun getRoles(): Flux<Role> = roleService.getRoles()

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: UUID): Mono<Role> = roleService.deleteRole(id)
}