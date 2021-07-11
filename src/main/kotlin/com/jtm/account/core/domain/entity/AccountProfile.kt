package com.jtm.account.core.domain.entity

import com.jtm.account.core.domain.dto.AccountProfileDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@Document("account_profile")
data class AccountProfile(
    @Id val id: UUID,
    var username: String,
    var email: String,
    var password: String,
    val roles: MutableList<Role>,
    var verified: Boolean
) {
    fun update(accountProfileDto: AccountProfileDto): AccountProfile {
        this.username = accountProfileDto.username!!
        this.email = accountProfileDto.email!!
        return this
    }

    fun setPassword(password: String, encoder: PasswordEncoder): AccountProfile {
        this.password = encoder.encode(password)
        return this
    }

    fun passwordMatches(password: String, encoder: PasswordEncoder): Boolean {
        return encoder.matches(password, this.password)
    }

    fun hasRole(priority: Int): Boolean {
        return this.roles.stream().anyMatch { it.priority == priority }
    }

    fun addRole(role: Role): AccountProfile {
        this.roles.add(role)
        return this
    }

    fun protectedView(): AccountProfile {
        this.password = ""
        return this
    }

    fun verified(): AccountProfile {
        this.verified = true
        return this
    }
}