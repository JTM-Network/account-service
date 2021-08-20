package com.jtm.account.core.domain.exception.account

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Password reset request not found.")
class PasswordResetNotFound: RuntimeException()