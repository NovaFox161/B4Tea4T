package pet.pupgrammer.b4tea4t.commands

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.EmbedService
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull

@Component
class InfoCommand(
    private val embedService: EmbedService,
): SlashCommand {
    override val name = "info"
    override val hasSubcommands = false
    override val ephemeral = false

    override suspend fun handle(event: ChatInputInteractionEvent) {
        val targetId = event.getOption("user")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .getOrNull()

        showLevel(event, targetId ?: event.interaction.user.id)
    }

    private suspend fun showLevel(event: ChatInputInteractionEvent, targetId: Snowflake) {
        val target = event.client.getMemberById(event.interaction.guildId.get(), targetId)
            .onErrorResume(ClientException.isStatusCode(404,403)) { Mono.empty() }
            .awaitSingleOrNull()
        if (target == null) {
            event.createFollowup("That user is not in this server anymore, sorry!").awaitSingleOrNull()
            return
        }

        event.createFollowup()
            .withEmbeds(embedService.generateLevelEmbed(target))
            .awaitSingleOrNull()
    }
}