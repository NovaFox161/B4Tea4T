package pet.pupgrammer.b4tea4t.`object`

import discord4j.common.util.Snowflake
import pet.pupgrammer.b4tea4t.database.WelcomeMessageData
import pet.pupgrammer.b4tea4t.extensions.toSnowflake

class WelcomeMessage(
    val guildId: Snowflake,
    val enabled: Boolean,
    val channelId: Snowflake,
    val messageContent: String?,
    val embedDescription: String?,
) {
    constructor(data: WelcomeMessageData) : this(
        guildId = data.guildId.toSnowflake(),
        enabled = data.enabled,
        channelId = data.channelId.toSnowflake(),
        messageContent = data.messageContent,
        embedDescription = data.embedDescription,
    )
}