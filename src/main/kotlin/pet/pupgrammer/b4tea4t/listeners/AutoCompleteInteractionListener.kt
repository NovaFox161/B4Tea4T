package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import pet.pupgrammer.b4tea4t.business.MetricService
import pet.pupgrammer.b4tea4t.interaction.InteractionHandler
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class AutoCompleteInteractionListener(
    private val handlers: List<InteractionHandler<ChatInputAutoCompleteEvent>>,
    private val metricService: MetricService,
) : EventListener<ChatInputAutoCompleteEvent> {
    override suspend fun handle(event: ChatInputAutoCompleteEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.respondWithSuggestions(listOf())
                .awaitSingleOrNull()
            return
        }

        // We use contains since this is meant to be re-usable but Discord uses the app command structure, so we have to handle that
        val id = "${event.commandName}.${event.focusedOption.name}"
        val handler = handlers.firstOrNull { it.ids.contains(id) }

        if (handler != null) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.error("Error handling auto complete interaction| id:$id | $event", e)

                // Attempt to respond with empty list so user doesn't have to wait
                event.respondWithSuggestions(listOf())
                    .awaitSingleOrNull()
            }
        } else {
            // No handler, respond empty until support is added
            event.respondWithSuggestions(listOf())
                .awaitSingleOrNull()
        }

        timer.stop()
        metricService.recordInteractionDuration(id, "auto-complete", timer.totalTimeMillis)
    }
}