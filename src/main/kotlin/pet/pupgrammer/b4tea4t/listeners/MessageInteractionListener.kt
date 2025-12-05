package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.MessageInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.interaction.InteractionHandler
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class MessageInteractionListener(
    private val commands: List<InteractionHandler<MessageInteractionEvent>>,
    private val metricService: MetricService,
): EventListener<MessageInteractionEvent> {
    override suspend fun handle(event: MessageInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply("Sorry, message commands are not supported in DMs").awaitSingleOrNull()
            return
        }

        val command = commands.firstOrNull { it.ids.contains(event.commandName) }

        if (command != null) {
            if (command.shouldDefer(event)) event.deferReply().withEphemeral(command.ephemeral).awaitSingleOrNull()

            try {
                command.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling message command | +${event.commandName} | $event", e)

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
        val computedInteractionName = "+${event.commandName}"
        metricService.recordInteractionDuration(computedInteractionName, "message-command", timer.totalTimeMillis)
    }
}