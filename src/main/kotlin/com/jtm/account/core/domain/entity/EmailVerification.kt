package com.jtm.account.core.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("email_verification")
data class EmailVerification(
    @Id val id: UUID,
    val token: String,
    val createdTime: Long,
    val endTime: Long) {

    fun isValid(): Boolean {
        return System.currentTimeMillis() < endTime
    }
}
