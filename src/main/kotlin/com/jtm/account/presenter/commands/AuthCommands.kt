package com.jtm.account.presenter.commands

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class AuthCommands: ListenerAdapter() {

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name != "auth") return
        val args = event.commandPath.split("/")
        event.reply(args.toString()).setEphemeral(true).queue()
        println(args.toString())
    }
}