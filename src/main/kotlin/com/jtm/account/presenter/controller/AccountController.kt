package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.account.AccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/account")
class AccountController @Autowired constructor(private val accountService: AccountService) {

    @GetMapping("/{id}")
    fun getAccount(@PathVariable id: UUID): Mono<AccountProfile> {
        return accountService.getAccount(id)
    }

    /**
     * Get all accounts
     */
    @GetMapping("/all")
    fun getAccounts(): Flux<AccountProfile> {
        return accountService.getAccounts()
    }
}