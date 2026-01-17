package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.ComponentService
import pet.pupgrammer.b4tea4t.business.EmbedService
import kotlin.math.max

@Component
class LeaderboardPrevPageButton(
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ButtonInteractionEvent> {
    override val ids = arrayOf("leaderboard-prev-")
    override val ephemeral = false

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.interaction.guild.awaitSingle()
        val currentPage = event.customId.split("-").last().toInt()
        val prevPage = max(0, currentPage - 1)

        event.message.get().edit()
            .withEmbeds(embedService.generateLevelLeaderboardEmbed(guild, prevPage))
            .withComponents(*componentService.getLeaderboardPaginationComponents(guild.id, prevPage))
            .awaitSingleOrNull()
    }
}