package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.account.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(private val profileService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody profileDto: AccountProfileDto): Mono<AccountProfile> = profileService.register(profileDto)

    @PostMapping("/login")
    fun login(@RequestBody profileDto: AccountProfileDto, response: ServerHttpResponse): Mono<String> = profileService.login(profileDto, response)

    @GetMapping("/me")
    fun me(request: ServerHttpRequest): Mono<AccountProfile> = profileService.whoami(request)

    @GetMapping("/refresh")
    fun refresh(request: ServerHttpRequest, res: ServerHttpResponse): Mono<String> = profileService.refresh(request, res)

    @GetMapping("/logout")
    fun logout(response: ServerHttpResponse): Mono<Void> = profileService.logout(response)
}