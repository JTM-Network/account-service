package com.jtm.account.presenter.controller

import com.jtm.account.data.service.account.VerifyService
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
import reactor.core.publisher.Mono
import java.util.*

@RunWith(SpringRunner::class)
@WebFluxTest(VerifyController::class)
@AutoConfigureWebTestClient
class VerifyControllerTest {

    @Autowired
    lateinit var testClient: WebTestClient

    @MockBean
    lateinit var verifyService: VerifyService

    @Test
    fun requestVerificationTest() {
        `when`(verifyService.requestVerification(anyOrNull())).thenReturn(Mono.empty())

        testClient.get()
            .uri("/verify/request")
            .exchange()
            .expectStatus().isOk

        verify(verifyService, times(1)).requestVerification(anyOrNull())
        verifyNoMoreInteractions(verifyService)
    }

    @Test
    fun getVerificationTest() {
        `when`(verifyService.getVerification(anyOrNull())).thenReturn(Mono.just("test"))

        testClient.get()
            .uri("/verify/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEqualTo("test")

        verify(verifyService, times(1)).getVerification(anyOrNull())
        verifyNoMoreInteractions(verifyService)
    }

    @Test
    fun confirmVerificationTest() {
        `when`(verifyService.confirmVerification(anyOrNull())).thenReturn(Mono.empty())

        testClient.get()
            .uri("/verify/confirm")
            .exchange()
            .expectStatus().isOk

        verify(verifyService, times(1)).confirmVerification(anyOrNull())
        verifyNoMoreInteractions(verifyService)
    }
}