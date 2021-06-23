package com.jtm.account.core.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.FOUND, reason = "Email or Username found.")
class EmailOrUsernameFound: RuntimeException()