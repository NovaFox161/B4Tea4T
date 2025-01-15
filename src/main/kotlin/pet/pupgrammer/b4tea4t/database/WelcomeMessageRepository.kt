package pet.pupgrammer.b4tea4t.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface WelcomeMessageRepository: R2dbcRepository<WelcomeMessageData, Long> {
    fun existsByGuildId(guildId: Long): Mono<Boolean>

    fun findByGuildId(guildId: Long): Mono<WelcomeMessageData>

    @Query("""
        UPDATE welcome_messages
        SET enabled = :enabled,
            channel_id = :channelId,
            message_content = :messageContent,
            embed_description = :embedDescription
            WHERE guild_id = :guildId
    """)
    fun updateByGuildId(
        guildId: Long,
        enabled: Boolean,
        channelId: Long,
        messageContent: String?,
        embedDescription: String?,
    ): Mono<Int>

    fun deleteByGuildId(guildId: Long): Mono<Long>
}