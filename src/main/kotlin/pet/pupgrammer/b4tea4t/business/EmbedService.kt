package pet.pupgrammer.b4tea4t.business

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Image
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.config.Config
import pet.pupgrammer.b4tea4t.utils.GlobalValues.iconUrl
import pet.pupgrammer.b4tea4t.utils.GlobalValues.levelEmbedColor
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.max

@Component
class EmbedService(
    private val levelService: LevelService,
    private val messageService: MessageService,
) {
    private val xpFormat = DecimalFormat("#.##")
    private val xpFormatLong = DecimalFormat("#.######")
    private val scoreFormat = DecimalFormat("#.###")

    init {
        xpFormat.roundingMode = RoundingMode.CEILING
    }

    suspend fun generateLevelLeaderboardEmbed(guild: Guild, page: Int): EmbedCreateSpec {
        val leaderboardPageSize = Config.LEVELING_LEADERBOARD_PAGE_SIZE.getInt()
        val leaders = levelService.getTopUsers(guild.id, page, leaderboardPageSize)
        val startingRank = levelService.getCurrentRank(guild.id, leaders.first().memberId)
        val pageCount = levelService.getLeaderboardPageCount(guild.id)
        val formattedLeaderboard = StringBuilder()

        leaders.forEachIndexed { index, userLevel ->
            val level = levelService.calculateLevelFromXp(userLevel.xp)
            formattedLeaderboard
                .append("${startingRank + index}. ")
                .append("<@${userLevel.memberId.asString()}>")
                .append(" - ")
                .append("${xpFormat.format(userLevel.xp)} XP ")
                .append("(Lvl $level)")
                .appendLine()
        }

        return EmbedCreateSpec.builder()
            .author("Leaderboard for ${guild.name}", null, guild.getIconUrl(Image.Format.PNG).orElse(iconUrl))
            .color(levelEmbedColor)
            .title("Members Sorted by XP")
            .description(formattedLeaderboard.toString())
            .footer("Page ${page + 1}/$pageCount", null)
            .timestamp(Instant.now())
            .build()
    }

    suspend fun generateLevelEmbed(member: Member): EmbedCreateSpec {
        val userLevel = levelService.getUserLevel(member.guildId, member.id)

        // Ensure the user is instantiated with level 0 and 0 XP if not already initialized
        val initializedUserLevel = if (userLevel.xp < 0) userLevel.copy(xp = 0f) else userLevel

        val currentRank = levelService.getCurrentRank(member.guildId, member.id)
        val currentLevel = levelService.calculateLevelFromXp(initializedUserLevel.xp)
        val xpToNextLevel = levelService.calculateXpToNextLevel(currentLevel)
        val xpToCurrentLevel = levelService.calculateXpToReachLevel(currentLevel)
        val currentXpAdjusted = initializedUserLevel.xp - xpToCurrentLevel

        val currentRateScore = levelService.calculateRateScore(member)
        val currentLongevityScore = levelService.calculateLongevityScore(member)
        val currentConsistencyScore = levelService.calculateConsistencyScore(member)
        val totalTrackedMessages = messageService.getTotalMessages(member.guildId, member.id)
        val daysActive = messageService.getDaysActive(member.guildId, member.id)
        val totalCalculatedWordCount = messageService.getTotalCalculatedWordCount(member.guildId, member.id).toFloat()
        val averageWordCount = if (totalTrackedMessages > 0) totalCalculatedWordCount / totalTrackedMessages else 0f
        val averageLengthScore = if (totalTrackedMessages > 0) {
            levelService.calculateLengthScore(averageWordCount.toInt(), hasMedia = false)
        } else {
            0f // Set to 0 if there are no messages
        }
        val guildIcon = member.guild.map { it.getIconUrl(Image.Format.PNG).orElse(iconUrl) }.awaitSingle()

        val levelAndProgressContent = StringBuilder()
            .appendLine("Level: `$currentLevel`")
            .appendLine("XP: `${xpFormat.format(max(0f, currentXpAdjusted))}/${xpToNextLevel.toInt()}`")
            .appendLine("Progress: `${generateXpProgressBar(max(0f, currentXpAdjusted), xpToNextLevel)}`")
            .toString()

        val engagementOverviewContent = StringBuilder()
            .appendLine("Total XP: `${xpFormat.format(initializedUserLevel.xp)}`")
            .appendLine("Messages: `$totalTrackedMessages`")
            .appendLine("Days Active: `$daysActive`")
            .appendLine("Avg. Word Count: `${averageWordCount.toInt()}`")
            .toString()

        val scoreSummaryContent = StringBuilder()
            .appendLine("Length: `${scoreFormat.format(averageLengthScore)}μ`")
            .appendLine("Rate: `${scoreFormat.format(currentRateScore)}`")
            .appendLine("Longevity: `${scoreFormat.format(currentLongevityScore)}`")
            .appendLine("Consistency: `${scoreFormat.format(currentConsistencyScore)}`")
            .toString()

        val averageMessageXp = if (totalTrackedMessages > 0) {
            xpFormatLong.format(initializedUserLevel.xp / totalTrackedMessages)
        } else {
            "N/A" // Not applicable if there are no messages
        }

        return EmbedCreateSpec.builder()
            .author("Tier #", null, guildIcon)
            .title("Rank #${currentRank}")
            .description("<@${member.id.asString()}>")
            .color(levelEmbedColor)
            .addField("Level & Progress", levelAndProgressContent, false)
            .addField("Engagement Overview", engagementOverviewContent, false)
            .addField("Score Summary", scoreSummaryContent, false)
            .addField("Average Message XP", "`$averageMessageXp`", false)
            .thumbnail(member.effectiveAvatarUrl)
            .timestamp(Instant.now())
            .build()
    }

    private fun generateXpProgressBar(currentXp: Float, xpToNextLevel: Float): String {
        val progressBarLength = 10
        val progressBarFill = max(0, ceil((currentXp / xpToNextLevel) * progressBarLength).toInt())

        return StringBuilder()
            .append("■".repeat(progressBarFill))
            .append("□".repeat(progressBarLength - progressBarFill))
            .append(" ${(currentXp / xpToNextLevel * 100).toInt()}%")
            .toString()
    }
}