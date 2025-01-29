package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.interaction.InteractionHandler
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class SelectMenuInteractionListener(
    private val dropdowns: List<InteractionHandler<SelectMenuInteractionEvent>>,
    private val metricService: MetricService,
) : EventListener<SelectMenuInteractionEvent> {

    override suspend fun handle(event: SelectMenuInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply("Sorry, but this interaction isn't supported in DMs").awaitSingleOrNull()
            return
        }

        val dropdown = dropdowns.firstOrNull { it.ids.contains(event.customId) }

        if (dropdown != null) {
            if (dropdown.shouldDefer(event)) event.deferReply().withEphemeral(dropdown.ephemeral).awaitSingleOrNull()

            try {
                dropdown.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling select menu interaction | id:${event.customId} | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup("Sorry, an unknown error has occurred. Please try again later.")
                    .withEphemeral(true)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup("Sorry, but that interaction was not found.")
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

        timer.stop()
        metricService.recordInteractionDuration(event.customId, "select-menu", timer.totalTimeMillis)
    }
}