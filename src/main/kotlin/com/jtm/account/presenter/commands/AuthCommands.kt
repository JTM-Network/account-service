package com.jtm.account.presenter.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kotlin.math.log

class AuthCommands: ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(AuthCommands::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        logger.info("Message received.")
        val split = event.message.contentRaw.split(" ")
        val cmd = split[0]
        val args = split.subList(1, split.size)
        logger.info("Checking....")
        if (!cmd.equals("auth", true)) return
        logger.info("Found...")
        event.author.openPrivateChannel()
                .map { it.sendMessage(args.toString()).queue() }
                .queue()
    }
}