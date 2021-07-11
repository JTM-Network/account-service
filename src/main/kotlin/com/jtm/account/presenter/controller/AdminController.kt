package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.account.AdminService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/admin")
class AdminController @Autowired constructor(private val adminService: AdminService) {

    @GetMapping("/{code}")
    fun makeAdmin(request: ServerHttpRequest, @PathVariable code: String): Mono<AccountProfile> = adminService.makeAdmin(request, code)
}