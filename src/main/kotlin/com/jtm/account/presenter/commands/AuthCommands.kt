package com.jtm.account.presenter.commands

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class AuthCommands: ListenerAdapter() {

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name != "auth") return
        val split = event.commandPath.split("/")
        val args = split.subList(1, split.size-1)
        event.reply(args.toString()).setEphemeral(true).queue()
        println(args.toString())
    }
}