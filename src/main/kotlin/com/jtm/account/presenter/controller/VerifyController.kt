package com.jtm.account.presenter.controller

import com.jtm.account.data.service.account.VerifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/verify")
class VerifyController @Autowired constructor(val verifyService: VerifyService) {

    @GetMapping("/request")
    fun requestVerification(request: ServerHttpRequest): Mono<Void> {
        return verifyService.requestVerification(request)
    }

    @GetMapping("/{id}")
    fun getVerification(@PathVariable id: UUID): Mono<String> {
        return verifyService.getVerification(id)
    }

    @GetMapping("/confirm")
    fun confirmVerification(request: ServerHttpRequest): Mono<Void> {
        return verifyService.confirmVerification(request)
    }
}