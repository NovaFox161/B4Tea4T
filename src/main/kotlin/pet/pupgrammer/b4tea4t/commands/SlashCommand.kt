package pet.pupgrammer.b4tea4t.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent


interface SlashCommand {
    val name: String
    val hasSubcommands: Boolean
    val ephemeral: Boolean

    suspend fun shouldDefer(event: ChatInputInteractionEvent): Boolean = true

    suspend fun handle(event: ChatInputInteractionEvent)
}