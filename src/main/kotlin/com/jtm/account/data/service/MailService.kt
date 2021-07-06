package com.jtm.account.data.service

import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MailService @Autowired constructor(private val mailClient: MailjetClient) {

    fun sendMail(request: MailjetRequest): Mono<MailjetResponse> {
        return Mono.just(mailClient.post(request))
    }
}