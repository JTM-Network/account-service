package com.jtm.account.presenter.controller

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.data.service.account.RoleService
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
@WebFluxTest(RoleController::class)
@AutoConfigureWebTestClient
class RoleControllerTest {

    @Autowired lateinit var testClient: WebTestClient

    @MockBean lateinit var roleService: RoleService

    @Test fun postRoleTest() {
        `when`(roleService.insertRole(anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 5)))

        testClient.post()
            .uri("/role")
            .bodyValue(RoleDto("test", 5))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.priority").isEqualTo(5)

        verify(roleService, times(1)).insertRole(anyOrNull())
        verifyNoMoreInteractions(roleService)
    }

    @Test fun putRoleTest() {
        `when`(roleService.updateRole(anyOrNull(), anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 6)))

        testClient.put()
            .uri("/role/" + UUID.randomUUID().toString())
            .bodyValue(RoleDto("test#4", 6))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.priority").isEqualTo(6)

        verify(roleService, times(1)).updateRole(anyOrNull(), anyOrNull())
        verifyNoMoreInteractions(roleService)
    }

    @Test fun getRoleTest() {
        `when`(roleService.getRole(anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 7)))

        testClient.get()
            .uri("/role/" + UUID.randomUUID().toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.priority").isEqualTo(7)

        verify(roleService, times(1)).getRole(anyOrNull())
        verifyNoMoreInteractions(roleService)
    }

    @Test fun getRolesTest() {
        `when`(roleService.getRoles()).thenReturn(Flux.just(Role(UUID.randomUUID(), "test", 2), Role(UUID.randomUUID(), "test#2", 4)))

        testClient.get()
            .uri("/role/all")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("test")
            .jsonPath("$[0].priority").isEqualTo(2)
            .jsonPath("$[1].name").isEqualTo("test#2")
            .jsonPath("$[1].priority").isEqualTo(4)

        verify(roleService, times(1)).getRoles()
        verifyNoMoreInteractions(roleService)
    }

    @Test fun deleteRoleTest() {
        `when`(roleService.deleteRole(anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 2)))

        testClient.delete()
            .uri("/role/" + UUID.randomUUID())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.priority").isEqualTo(2)

        verify(roleService, times(1)).deleteRole(anyOrNull())
        verifyNoMoreInteractions(roleService)
    }
}