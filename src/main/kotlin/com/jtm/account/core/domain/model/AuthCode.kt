package com.jtm.account.core.domain.model

import com.jtm.account.core.util.UtilString

data class AuthCode(
    val code: String = UtilString.randomString(6),
    var used: Boolean = false,
) {
    fun used() {
        this.used = true
    }
}
