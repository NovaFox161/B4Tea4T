package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.TextInput
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.EchoService

@Component
class EchoMessageModal(
    private val echoService: EchoService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("echo")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent) {
        val guildId = event.interaction.guildId.get()
        val inputs = event.getComponents(TextInput::class.java)

        val messageContent = inputs.first { it.customId == "echo.message-content" }.value.get()

        // TODO: get channel and message reference ID from custom ID

        // TODO: Send message via service
    }
}