package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.commands.SlashCommand
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class SlashCommandListener(
    private val commands: List<SlashCommand>,
    private val metricService: MetricService,
): EventListener<ChatInputInteractionEvent> {
    override suspend fun handle(event: ChatInputInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply("Sorry, commands are not supported in DMs").awaitSingleOrNull()
            return
        }

        val command = commands.firstOrNull { it.name == event.commandName }
        val subCommand = if (command?.hasSubcommands == true) event.options[0].name else null

        if (command != null) {
            if (command.shouldDefer(event)) event.deferReply().withEphemeral(command.ephemeral).awaitSingleOrNull()

            try {
                command.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling slash command | /${event.commandName} sub:${subCommand} | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup("Sorry, an unknown error has occurred. Please try again later.")
                    .withEphemeral(command.ephemeral)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup("Sorry, but that command was not found.")
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

        timer.stop()
        val computedInteractionName = if (subCommand != null) "/${event.commandName}#$subCommand" else "/${event.commandName}"
        metricService.recordInteractionDuration(computedInteractionName, "chat-input", timer.totalTimeMillis)
    }
}