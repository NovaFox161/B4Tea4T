package pet.pupgrammer.b4tea4t.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.CleanupService
import pet.pupgrammer.b4tea4t.business.ComponentService
import pet.pupgrammer.b4tea4t.business.EmbedService

@Component
class LeaderboardCommand(
    private val embedService: EmbedService,
    private val componentService: ComponentService,
    private val cleanupService: CleanupService,
): SlashCommand {
    override val name = "leaderboard"
    override val hasSubcommands = false
    override val ephemeral = false

    override suspend fun handle(event: ChatInputInteractionEvent) {
        val guild = event.interaction.guild.awaitSingle()

        // Check if cleanup is needed
        if (cleanupService.isCleanupNeeded()) cleanupService.cleanUpInactiveMembers(guild)

        // Generate and display the leaderboard
        event.createFollowup()
            .withEmbeds(embedService.generateLevelLeaderboardEmbed(guild, page = 0))
            .withComponents(*componentService.getLeaderboardPaginationComponents(guild.id, currentPage = 0))
            .awaitSingleOrNull()
    }
}