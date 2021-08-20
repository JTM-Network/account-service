package com.jtm.account.core.domain.exception.token

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Api token is invalid.")
class InvalidApiToken: RuntimeException()