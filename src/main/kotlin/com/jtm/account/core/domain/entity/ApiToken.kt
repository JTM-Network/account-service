package com.jtm.account.core.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("api_tokens")
data class ApiToken(
    @Id val id: UUID = UUID.randomUUID(),
    val token: String,
    val accountId: UUID,
    var valid: Boolean = true,
    val created: Long = System.currentTimeMillis()) {

    fun invalidate(): ApiToken {
        this.valid = false
        return this
    }
}