package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.entity.ApiToken
import com.jtm.account.data.service.ApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api-token")
class ApiController @Autowired constructor(private val apiService: ApiService) {

    @PostMapping
    fun createToken(request: ServerHttpRequest): Mono<ApiToken> {
        return apiService.createToken(request)
    }

    @GetMapping("/{id}")
    fun getToken(@PathVariable id: UUID): Mono<ApiToken> {
        return apiService.getToken(id)
    }

    @GetMapping("/account")
    fun getAccount(request: ServerHttpRequest): Mono<AccountProfile> {
        return apiService.getAccount(request)
    }

    @GetMapping("/tokens/{id}")
    fun getTokensByAccountId(@PathVariable id: UUID): Flux<ApiToken> {
        return apiService.getTokensByAccountId(id)
    }

    @GetMapping("/tokens")
    fun getTokens(request: ServerHttpRequest): Flux<ApiToken> {
        return apiService.getTokens(request)
    }

    @DeleteMapping("/{id}")
    fun blacklistToken(request: ServerHttpRequest): Mono<ApiToken> {
        return apiService.blacklistToken(request)
    }
}