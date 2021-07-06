package com.jtm.account.core.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import java.util.concurrent.TimeUnit

@Document("password_reset")
data class PasswordReset(
    @Id val id: UUID = UUID.randomUUID(),
    val token: String,
    val email: String,
    val used: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    val expire: Long = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
) {
    fun valid(): Boolean {
        return !used || System.currentTimeMillis() < expire
    }
}