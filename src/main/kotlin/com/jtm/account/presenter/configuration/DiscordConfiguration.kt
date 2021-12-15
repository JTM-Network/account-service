package com.jtm.account.presenter.configuration

import com.jtm.account.presenter.commands.AuthCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DiscordConfiguration {

    @Value("\${discord.token:token}")
    lateinit var token: String

    @Bean
    open fun discordBot(): JDA {
        val builder = JDABuilder.createDefault(token)
        builder.addEventListeners(AuthCommands())
        builder.setActivity(Activity.streaming("JTM Network", "https://www.jtm-network.com"))
        builder.setStatus(OnlineStatus.IDLE)
//        val jda = builder.build()
//        jda.upsertCommand("auth", "Authenticate discord account to JTM Network account.").queue()
        return builder.build()
    }
}