package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.interaction.InteractionHandler
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class ButtonInteractionListener(
    private val buttons: List<InteractionHandler<ButtonInteractionEvent>>,
    private val metricService: MetricService,
) : EventListener<ButtonInteractionEvent> {

    override suspend fun handle(event: ButtonInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply("Sorry, buttons are not supported in DMs").awaitSingleOrNull()
            return
        }

        val button = buttons.firstOrNull { it.ids.contains(event.customId) }

        if (button != null) {
            try {
                if (button.shouldDefer(event)) event.deferReply().withEphemeral(button.ephemeral).awaitSingleOrNull()

                button.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling button interaction | id:${event.customId} | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup("Sorry, there was an unknown error, please try again later.")
                    .withEphemeral(true)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup("Sorry, that button was not found.")
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

        timer.stop()
        metricService.recordInteractionDuration(event.customId, "button", timer.totalTimeMillis)
    }
}