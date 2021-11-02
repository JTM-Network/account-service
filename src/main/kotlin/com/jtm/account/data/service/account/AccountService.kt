package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class AccountService @Autowired constructor(private val profileRepository: AccountProfileRepository) {

    fun getAccount(id: UUID): Mono<AccountProfile> {
        return profileRepository.findById(id)
    }

    fun getAccounts(): Flux<AccountProfile> {
        return profileRepository.findAll()
    }
}