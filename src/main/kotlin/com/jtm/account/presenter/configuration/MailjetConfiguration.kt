package com.jtm.account.presenter.configuration

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MailjetConfiguration {

    @Value("\${jtm.mail.apiKey:apiKey}")
    lateinit var apiKey: String

    @Value("\${jtm.mail.apiSecret:apiSecret}")
    lateinit var apiSecret: String

    @Bean
    open fun mailJetClient(): MailjetClient {
        return MailjetClient(apiKey, apiSecret, ClientOptions("v3.1"))
    }
}