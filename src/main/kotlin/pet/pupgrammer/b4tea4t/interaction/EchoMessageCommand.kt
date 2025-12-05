package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.MessageInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.ComponentService

@Component
class EchoMessageCommand(
    private val componentService: ComponentService,
): InteractionHandler<MessageInteractionEvent> {
    override val ids = arrayOf("Echo (Reply)")
    override val ephemeral = true

    override suspend fun shouldDefer(event: MessageInteractionEvent) = false

    override suspend fun handle(event: MessageInteractionEvent) {
        val channelId = event.interaction.channelId
        val messageId = event.targetId

        event.presentModal()
            .withCustomId("echo.${channelId.asString()}.${messageId.asString()}")
            .withTitle("Echo")
            .withComponents(*componentService.getEchoModalComponents())
            .awaitSingleOrNull()
    }

}