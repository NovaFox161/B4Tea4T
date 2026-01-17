package pet.pupgrammer.b4tea4t.`object`

import discord4j.common.util.Snowflake
import pet.pupgrammer.b4tea4t.database.MessageRecordData
import pet.pupgrammer.b4tea4t.extensions.toSnowflake
import java.time.Instant
import java.time.ZoneOffset

data class MessageRecord(
    val messageId: Snowflake,
    val guildId: Snowflake,
    val memberId: Snowflake,
    val channelId: Snowflake,
    val wordCount: Int,
    val dayBucket: Instant,
) {
    constructor(data: MessageRecordData): this(
        messageId = data.messageId.toSnowflake(),
        guildId = data.guildId.toSnowflake(),
        memberId = data.memberId.toSnowflake(),
        channelId = data.channelId.toSnowflake(),
        wordCount = data.wordCount,
        dayBucket = data.dayBucket.atStartOfDay().toInstant(ZoneOffset.UTC),
    )
}