package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.AllowedMentions
import discord4j.rest.util.Color
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.WelcomeMessageCache
import pet.pupgrammer.b4tea4t.database.WelcomeMessageData
import pet.pupgrammer.b4tea4t.database.WelcomeMessageRepository
import pet.pupgrammer.b4tea4t.extensions.embedDescriptionSafe
import pet.pupgrammer.b4tea4t.extensions.messageContentSafe
import pet.pupgrammer.b4tea4t.logger.LOGGER
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage
import reactor.core.publisher.Mono

@Component
class WelcomeMessageService(
    private val repository: WelcomeMessageRepository,
    private val cache: WelcomeMessageCache,
    private val metricService: MetricService,
) {

    //////////////////////
    /// CRUD functions ///
    //////////////////////
    suspend fun createWelcomeMessage(message: WelcomeMessage): WelcomeMessage {
        if (hasWelcomeMessage(message.guildId)) throw IllegalStateException("Guild already has welcome message, update instead")

        val confirmed = repository.save(WelcomeMessageData(
            guildId = message.guildId.asLong(),
            enabled = message.enabled,
            channelId = message.channelId.asLong(),
            messageContent = message.messageContent,
            embedDescription = message.embedDescription,
        )).map(::WelcomeMessage).awaitSingle()
        cache.put(key = confirmed.guildId, value = confirmed)

        return confirmed
    }

    suspend fun hasWelcomeMessage(guildId: Snowflake): Boolean {
        return repository.existsByGuildId(guildId.asLong()).awaitSingle()
    }

    suspend fun getWelcomeMessage(guildId: Snowflake): WelcomeMessage? {
        var message = cache.get(key = guildId)
        if (message != null) return message

        message = repository.findByGuildId(guildId.asLong())
            .map(::WelcomeMessage)
            .awaitSingle()
        if (message != null) cache.put(key = guildId, value = message)

        return message
    }

    suspend fun updateWelcomeMessage(message: WelcomeMessage): WelcomeMessage {

        repository.updateByGuildId(
            guildId = message.guildId.asLong(),
            enabled = message.enabled,
            channelId = message.channelId.asLong(),
            messageContent = message.messageContent,
            embedDescription = message.embedDescription,
        ).awaitSingleOrNull()
        cache.put(key = message.guildId, value = message)

        return message
    }

    suspend fun upsertWelcomeMessage(message: WelcomeMessage): WelcomeMessage {
        return if (hasWelcomeMessage(message.guildId)) updateWelcomeMessage(message)
        else createWelcomeMessage(message)
    }

    suspend fun deleteWelcomeMessage(guildId: Snowflake) {
        repository.deleteByGuildId(guildId.asLong()).awaitSingleOrNull()
        cache.evict(key = guildId)
    }

    ///////////////////////////
    /// Discord Integration ///
    ///////////////////////////
    suspend fun postWelcomeMessage(guildId: Snowflake, member: Member) {
        val message = getWelcomeMessage(guildId) ?: return
        if (!message.enabled) return
        if (message.messageContent.isNullOrBlank() && message.embedDescription.isNullOrBlank()) return

        val guild = member.guild.awaitSingle()
        val channel = guild.getChannelById(message.channelId)
            .ofType(GuildMessageChannel::class.java)
            .doOnError { LOGGER.error("Failed to retrieve channel for welcome message post", it) }
            .onErrorResume { Mono.empty() } // We can just silently fail if this disappears, not going to run this at scale
            .awaitSingleOrNull() ?: return

        // Parse messages for placeholders
        val computedMessage = message.messageContent
            ?.replace("%user.mention%", member.mention)
            ?.replace("%guild.name%", guild.name)
            ?.messageContentSafe()

        val computedEmbedDescription = message.embedDescription
            ?.replace("%user.mention%", member.mention)
            ?.replace("%guild.name%", guild.name)
            ?.embedDescriptionSafe()

        val embed = if (!computedEmbedDescription.isNullOrBlank()) {
            EmbedCreateSpec.builder()
                .color(Color.GRAY_CHATEAU)
                .description(computedEmbedDescription)
                .build()
        } else null

        channel.createMessage(computedMessage ?: "")
            .withEmbeds(embed)
            .withAllowedMentions(AllowedMentions.builder()
                .allowUser(member.id)
                .build()
            ).awaitSingleOrNull()

        metricService.incrementWelcomeMessagesPosted()
    }
}