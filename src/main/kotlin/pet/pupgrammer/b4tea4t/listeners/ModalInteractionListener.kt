package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.interaction.InteractionHandler
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class ModalInteractionListener(
    private val modals: List<InteractionHandler<ModalSubmitInteractionEvent>>,
    private val metricService: MetricService,
) : EventListener<ModalSubmitInteractionEvent> {

    override suspend fun handle(event: ModalSubmitInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply("Sorry, but this interaction isn't supported in DMs").awaitSingleOrNull()
            return
        }

        val modal = modals.firstOrNull { it.ids.any(event.customId::startsWith) }

        if (modal != null) {
            try {
                if (modal.shouldDefer(event)) event.deferReply().withEphemeral(modal.ephemeral).awaitSingleOrNull()

                modal.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling modal interaction | ${event.customId} | $event", e)

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
        metricService.recordInteractionDuration(modal?.ids?.joinToString("|") ?: event.customId, "button", timer.totalTimeMillis)
    }
}