package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.rest.util.Image
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.logger.LOGGER
import pet.pupgrammer.b4tea4t.utils.GlobalValues
import pet.pupgrammer.b4tea4t.utils.GlobalValues.STATUS

@Component
class ReadyEventListener: EventListener<ReadyEvent> {
    override suspend fun handle(event: ReadyEvent) {
        LOGGER.info(STATUS, "Ready Event  ${event.sessionId}")

        val iconUrl = event.client.applicationInfo
            .map { it.getIconUrl(Image.Format.PNG).orElse("") }
            .awaitSingle()
        GlobalValues.iconUrl = iconUrl
    }
}