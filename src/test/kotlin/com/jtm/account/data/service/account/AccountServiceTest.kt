package com.jtm.account.data.service.account

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.domain.exception.account.AccountNotFound
import com.jtm.account.core.usecase.repository.AccountProfileRepository
import org.assertj.core.api.Assertions.`as`
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class AccountServiceTest {

    private val profileRepository: AccountProfileRepository = mock()
    private val accountService = AccountService(profileRepository)

    private val profile = AccountProfile(UUID.randomUUID(), "test", "test@gmail.com", "pass", mutableListOf(), false)

    @Test
    fun getAccount_thenNotFound() {
        `when`(profileRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = accountService.getAccount(UUID.randomUUID())

        verify(profileRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .expectError(AccountNotFound::class.java)
                .verify()
    }

    @Test
    fun getAccount() {
        `when`(profileRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(profile))

        val returned = accountService.getAccount(UUID.randomUUID())

        verify(profileRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .assertNext {
                    assertThat(it.id).isEqualTo(profile.id)
                    assertThat(it.email).isEqualTo(profile.email)
                }
                .verifyComplete()
    }

    @Test
    fun getAccounts() {
        `when`(profileRepository.findAll()).thenReturn(Flux.just(profile))

        val returned = accountService.getAccounts()

        verify(profileRepository, times(1)).findAll()
        verifyNoMoreInteractions(profileRepository)

        StepVerifier.create(returned)
                .assertNext {
                    assertThat(it.id).isEqualTo(profile.id)
                    assertThat(it.username).isEqualTo(profile.username)
                    assertThat(it.email).isEqualTo(profile.email)
                }
                .verifyComplete()
    }
}