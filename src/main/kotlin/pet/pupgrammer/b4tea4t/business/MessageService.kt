package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.CategorizableChannel
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.`object`.entity.channel.ThreadChannel
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils
import pet.pupgrammer.b4tea4t.DaysActiveCache
import pet.pupgrammer.b4tea4t.MessageRecordCache
import pet.pupgrammer.b4tea4t.config.Config
import pet.pupgrammer.b4tea4t.database.MessageRecordData
import pet.pupgrammer.b4tea4t.database.MessageRecordRepository
import pet.pupgrammer.b4tea4t.extensions.isThread
import pet.pupgrammer.b4tea4t.logger.LOGGER
import pet.pupgrammer.b4tea4t.`object`.MessageRecord
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class MessageService(
    private val messageRecordRepository: MessageRecordRepository,
    private val messageRecordCache: MessageRecordCache,
    @Qualifier("daysActiveCache")
    private val daysActiveCache: DaysActiveCache,
) {

    //////////////////////////////////
    /// Message leveling functions ///
    //////////////////////////////////
    suspend fun qualifiesForLeveling(message: Message): Boolean {
        val ignoredChannels = Config.LEVELING_IGNORED_CHANNELS.getString()
            .split(",")
            .filter(String::isNotBlank)
            .map(Snowflake::of)
        val trackedRoles = Config.LEVELING_TRACKED_ROLES.getString()
            .split(",")
            .filter(String::isNotBlank)
            .map(Snowflake::of)

        // Filter messages that will never qualify
        if (!message.guildId.isPresent) return false
        if (ignoredChannels.contains(message.channelId)) return false

        // Check author requirements
        val author = message.authorAsMember.awaitSingleOrNull() ?: return false
        if (author.isBot) return false
        if (trackedRoles.isNotEmpty() && !CollectionUtils.containsAny(author.roleIds, trackedRoles)) return false


        // Check if channel is in ignored category
        val channelType = message.channel.ofType(Channel::class.java).awaitSingle().type
        if (channelType.isThread()) {
            // Check if thread is in ignored channel
            val thread = message.channel.ofType(ThreadChannel::class.java).awaitSingle()
            if (ignoredChannels.contains(thread.parentId.get())) return false

            // Check if parent is in ignored category
            val parent = thread.client
                .getChannelById(thread.parentId.get())
                .ofType(CategorizableChannel::class.java)
                .awaitSingle()
            if (parent.categoryId.map(ignoredChannels::contains).orElse(false)) return false
        } else {
            // Can be categorized, check if category is ignored
            val channel = message.channel.ofType(TextChannel::class.java).awaitSingle()
            if (channel.categoryId.map(ignoredChannels::contains).orElse(false)) return false
        }

        return true
    }

    ////////////////////////////////
    /// Message Record functions ///
    ////////////////////////////////
    suspend fun recordMessage(message: Message): MessageRecord {
        LOGGER.debug("Creating message record for message ${message.id.asString()}")

        val messageRecord = messageRecordRepository.save(
            MessageRecordData(
                messageId = message.id.asLong(),
                guildId = message.guildId.get().asLong(),
                memberId = message.authorAsMember.awaitSingle().id.asLong(),
                channelId = message.channelId.asLong(),
                wordCount = message.content.trim().split(regex = Regex("\\s+")).size,
                dayBucket = LocalDate.ofInstant(message.timestamp, ZoneOffset.UTC)
            )
        ).map(::MessageRecord).awaitSingle()

        messageRecordCache.put(message.guildId.get(), key = message.id, messageRecord)

        return messageRecord
    }

    suspend fun getDaysActive(guildId: Snowflake, memberId: Snowflake): Long {
        var daysActive = daysActiveCache.get(guildId, memberId)
        if (daysActive != null) return daysActive

        daysActive = messageRecordRepository.countDaysActiveByMemberIdAndGuildId(
            memberId = memberId.asLong(),
            guildId = guildId.asLong()
        ).awaitSingleOrNull() ?: 0

        daysActiveCache.put(guildId, memberId, daysActive)

        return daysActive
    }

    suspend fun getMessagesPerHour(guildId: Snowflake, memberId: Snowflake, start: Instant, end: Instant = Instant.now()): Float {
        val lookbackStart = Snowflake.of(start)
        val lookbackEnd = Snowflake.of(end)
        val lookbackWindow = Duration.between(start, end).toHours()

        // TODO: I wonder if there's a way to cache this kind of thing
        val totalMessages = messageRecordRepository.countByMemberIdAndGuildIdAndMessageIdGreaterThanEqualAndMessageIdLessThanEqual(
            memberId = memberId.asLong(),
            guildId = guildId.asLong(),
            startMessageId = lookbackStart.asLong(),
            endMessageId = lookbackEnd.asLong()
        ).awaitSingleOrNull() ?: 0

        // Average messages per hour over the lookback window
        return totalMessages.toFloat() / lookbackWindow.toFloat()
    }

    suspend fun getTotalMessages(guildId: Snowflake, memberId: Snowflake): Long {
        return messageRecordRepository.countByGuildIdAndMemberId(
            guildId = guildId.asLong(),
            memberId = memberId.asLong()
        ).awaitSingleOrNull() ?: 0
    }

    suspend fun getTotalCalculatedWordCount(guildId: Snowflake, memberId: Snowflake): Long {
        return messageRecordRepository.sumWordsByGuildIdAndMemberId(
            guildId = guildId.asLong(),
            memberId = memberId.asLong()
        ).awaitSingleOrNull() ?: 0
    }
}