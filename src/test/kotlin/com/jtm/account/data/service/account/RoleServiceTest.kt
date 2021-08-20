package com.jtm.account.data.service.account

import com.jtm.account.core.domain.dto.RoleDto
import com.jtm.account.core.domain.entity.Role
import com.jtm.account.core.domain.exception.account.RoleFound
import com.jtm.account.core.domain.exception.account.RoleNotFound
import com.jtm.account.core.usecase.repository.RoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@RunWith(SpringRunner::class)
class RoleServiceTest {

    private val roleRepository = mock(RoleRepository::class.java)
    private val roleService = RoleService(roleRepository)

    @Test fun insertRoleTest() {
        `when`(roleRepository.findByName(anyString())).thenReturn(Mono.empty())
        `when`(roleRepository.save(anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 1)))

        val returned = roleService.insertRole(RoleDto("test", 0))

        verify(roleRepository, times(1)).findByName(anyString())
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.name).isEqualTo("test")
                assertThat(it.priority).isEqualTo(1)
            }
            .verifyComplete()
    }

    @Test fun insertRole_thenFoundTest() {
        `when`(roleRepository.findByName(anyString())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 0)))

        val returned = roleService.insertRole(RoleDto("test", 0))

        verify(roleRepository, times(1)).findByName(anyString())
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .expectError(RoleFound::class.java)
            .verify()
    }

    @Test fun updateRoleTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(Role(UUID.randomUUID(), "test#1", 2)))
        `when`(roleRepository.save(anyOrNull())).thenReturn(Mono.just(Role(UUID.randomUUID(), "test#3", 10)))

        val returned = roleService.updateRole(UUID.randomUUID(), RoleDto("role", 10))

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.name).isEqualTo("test#3")
                assertThat(it.priority).isEqualTo(10)
            }
            .verifyComplete()
    }

    @Test fun updateRole_thenNotFoundTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = roleService.updateRole(UUID.randomUUID(), RoleDto("role", 32))

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .expectError(RoleNotFound::class.java)
            .verify()
    }

    @Test fun getRoleTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(Role(UUID.randomUUID(), "test#3", 6)))

        val returned = roleService.getRole(UUID.randomUUID())

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.name).isEqualTo("test#3")
                assertThat(it.priority).isEqualTo(6)
            }
            .verifyComplete()
    }

    @Test fun getRole_thenNotFoundTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = roleService.getRole(UUID.randomUUID())

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .expectError(RoleNotFound::class.java)
            .verify()
    }

    @Test fun getRolesTest() {
        `when`(roleRepository.findAll()).thenReturn(Flux.just(Role(UUID.randomUUID(), "test#4", 5), Role(UUID.randomUUID(), "test#5", 7)))

        val returned = roleService.getRoles()

        verify(roleRepository, times(1)).findAll()
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.name).isEqualTo("test#4")
                assertThat(it.priority).isEqualTo(5)
            }
            .assertNext {
                assertThat(it.name).isEqualTo("test#5")
                assertThat(it.priority).isEqualTo(7)
            }
            .verifyComplete()
    }

    @Test fun deleteRoleTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.just(Role(UUID.randomUUID(), "test", 13)))
        `when`(roleRepository.delete(anyOrNull())).thenReturn(Mono.empty())

        val returned = roleService.deleteRole(UUID.randomUUID())

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .assertNext {
                assertThat(it.name).isEqualTo("test")
                assertThat(it.priority).isEqualTo(13)
            }
            .verifyComplete()
    }

    @Test fun deleteRole_thenNotFoundTest() {
        `when`(roleRepository.findById(any(UUID::class.java))).thenReturn(Mono.empty())

        val returned = roleService.deleteRole(UUID.randomUUID())

        verify(roleRepository, times(1)).findById(any(UUID::class.java))
        verifyNoMoreInteractions(roleRepository)

        StepVerifier.create(returned)
            .expectError(RoleNotFound::class.java)
            .verify()
    }
}