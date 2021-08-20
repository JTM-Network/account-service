package com.jtm.account.core.domain.exception.account

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Password can't be empty.")
class InvalidPassword: RuntimeException()