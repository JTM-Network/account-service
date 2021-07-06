package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.AccountProfileDto
import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.MailService
import com.jtm.account.data.service.account.PasswordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/forgot-password")
class PasswordController @Autowired constructor(val passwordService: PasswordService,
                                                val mailService: MailService) {

    @GetMapping("/request")
    fun forgotPasswordRequest(@RequestParam("email") email: String): Mono<Void> {
        return passwordService.requestForgotPasswordReset(email, mailService)
    }

    @GetMapping("/{id}")
    fun getRequest(@PathVariable id: UUID): Mono<String> {
        return passwordService.getForgotPasswordToken(id)
    }

    @PostMapping("/reset")
    fun resetPassword(request: ServerHttpRequest, @RequestBody profileDto: AccountProfileDto): Mono<Void> {
        return passwordService.resetPassword(request, profileDto)
    }
}