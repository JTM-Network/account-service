package com.jtm.account.core.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("email_verification")
data class EmailVerification(
    @Id val id: UUID = UUID.randomUUID(),
    val email: String,
    val token: String,
    var confirmed: Boolean = false,
    val createdTime: Long,
    val endTime: Long) {

    fun isValid(): Boolean {
        return System.currentTimeMillis() < endTime
    }

    fun confirmed(): EmailVerification {
        this.confirmed = true
        return this
    }
}
