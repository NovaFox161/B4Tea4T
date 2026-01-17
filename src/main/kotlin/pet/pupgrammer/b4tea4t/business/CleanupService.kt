package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

@Component
class CleanupService(
    private val levelService: LevelService,
) {
    // AtomicLong to store the last cleanup timestamp to ensure thread-safety
    private val nextCleanup = AtomicLong(0)

    // Interval between cleanups
    private val cleanupInterval = Duration.ofDays(7)

    /**
     * Checks if cleanup is needed based on the interval
     * @return true if cleanup is needed, false otherwise
     */
    fun isCleanupNeeded(): Boolean {
        val currentTime = Instant.now()
        val scheduledCleanup = Instant.ofEpochMilli(nextCleanup.get())
        return currentTime.isAfter(scheduledCleanup)
    }

    /**
     * Cleans up all inactive members in the guild
     * @param guild The guild to clean up members for
     */
    suspend fun cleanUpInactiveMembers(guild: Guild) {
        nextCleanup.set(Instant.now().plus(cleanupInterval).toEpochMilli())

        // Iterate over the user levels to check if the corresponding members are still in the guild
        levelService.getAllUserLevelsNoCache(guild.id).forEach { removeInactiveMember(guild, it.memberId) }
    }

    /**
     * Removes an inactive member
     * @param guild The guild to remove the user from
     * @param userId The ID of the user to remove
     * @param members An optional list of pre-fetched guild members
     */
    suspend fun removeInactiveMember(guild: Guild, userId: Snowflake, members: List<Member>? = null) {
        val member = members?.find { it.id == userId }
            ?: guild.getMemberById(userId)
                .onErrorResume(ClientException.isStatusCode(404)) { Mono.empty() }
                .awaitSingleOrNull()

        // If the member is not found, remove the user level from the repository
        if (member == null) levelService.removeInactiveMemberLevel(guild.id, userId)
    }
}