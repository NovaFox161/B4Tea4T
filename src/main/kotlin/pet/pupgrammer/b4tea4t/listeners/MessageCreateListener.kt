package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.LevelService
import pet.pupgrammer.b4tea4t.business.MessageService
import pet.pupgrammer.b4tea4t.logger.LOGGER

@Component
class MessageCreateListener(
    private val messageService: MessageService,
    private val levelService: LevelService,
): EventListener<MessageCreateEvent> {

    override suspend fun handle(event: MessageCreateEvent) {
        try {
            handleLeveling(event)
        } catch (e: Exception) {
            LOGGER.error("Error handling message create event", e)
        }
    }

    private suspend fun handleLeveling(event: MessageCreateEvent) {
        if (messageService.qualifiesForLeveling(event.message)) {
            LOGGER.debug("Message qualifies for leveling - ${event.message.id.asString()}")
            messageService.recordMessage(event.message)
            levelService.handleQualifyingMessage(event.message)
        }
    }
}