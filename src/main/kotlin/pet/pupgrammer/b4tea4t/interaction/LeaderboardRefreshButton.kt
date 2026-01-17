package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.ComponentService
import pet.pupgrammer.b4tea4t.business.EmbedService
import pet.pupgrammer.b4tea4t.business.LevelService
import kotlin.math.max
import kotlin.math.min

@Component
class LeaderboardRefreshButton(
    private val levelService: LevelService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ButtonInteractionEvent> {
    override val ids = arrayOf("leaderboard-refresh-")
    override val ephemeral = false

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.interaction.guild.awaitSingle()
        val currentPage = event.customId.split("-").last().toInt()
        val totalPages = levelService.getLeaderboardPageCount(guild.id)

        val page = max(0, min(currentPage, totalPages - 1))

        event.message.get().edit()
            .withEmbeds(embedService.generateLevelLeaderboardEmbed(guild, page))
            .withComponents(*componentService.getLeaderboardPaginationComponents(guild.id, page))
            .awaitSingleOrNull()
    }
}