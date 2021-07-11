package com.jtm.account.core.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Code already been used.")
class CodeAlreadyUsed: RuntimeException()