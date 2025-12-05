package pet.pupgrammer.b4tea4t.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.ComponentService

@Component
class EchoCommand(
    private val componentService: ComponentService,
): SlashCommand {
    override val name = "echo"
    override val hasSubcommands = false
    override val ephemeral = true

    override suspend fun shouldDefer(event: ChatInputInteractionEvent) = false

    override suspend fun handle(event: ChatInputInteractionEvent) {
        val channelId = event.interaction.channelId

        event.presentModal()
            .withCustomId("echo.${channelId.asString()}")
            .withTitle("Echo")
            .withComponents(*componentService.getEchoModalComponents())
            .awaitSingleOrNull()
    }
}