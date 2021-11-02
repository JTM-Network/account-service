package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.data.service.account.AdminService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RunWith(SpringRunner::class)
@WebFluxTest(AdminController::class)
@AutoConfigureWebTestClient
class AdminControllerTest {

    @Autowired
    lateinit var testClient: WebTestClient

    @MockBean
    lateinit var adminService: AdminService

    private val account = AccountProfile(UUID.randomUUID(), "test", "email", "", mutableListOf(), false)

    @Test
    fun makeAdminTest() {
        `when`(adminService.makeAdmin(anyOrNull(), anyOrNull())).thenReturn(Mono.just(AccountProfile(UUID.randomUUID(), "test", "email", "", mutableListOf(), false)))

        testClient.get()
            .uri("/admin/test")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("test")
            .jsonPath("$.email").isEqualTo("email")

        verify(adminService, times(1)).makeAdmin(anyOrNull(), anyOrNull())
        verifyNoMoreInteractions(adminService)
    }

    @Test
    fun getAccountTest() {
        `when`(adminService.getAccount(anyOrNull())).thenReturn(Mono.just(account))

        testClient.get()
                .uri("/admin/acc/${UUID.randomUUID()}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(account.id.toString())
                .jsonPath("$.username").isEqualTo(account.username)
                .jsonPath("$.email").isEqualTo(account.email)

        verify(adminService, times(1)).getAccount(anyOrNull())
        verifyNoMoreInteractions(adminService)
    }

    @Test
    fun getAccountsTest() {
        `when`(adminService.getAccounts()).thenReturn(Flux.just(account))

        testClient.get()
                .uri("/admin/acc/all")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(account.id.toString())
                .jsonPath("$[0].username").isEqualTo(account.username)
                .jsonPath("$[0].email").isEqualTo(account.email)

        verify(adminService, times(1)).getAccounts()
        verifyNoMoreInteractions(adminService)
    }
}