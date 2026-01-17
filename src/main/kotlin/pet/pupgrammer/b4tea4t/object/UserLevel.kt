package pet.pupgrammer.b4tea4t.`object`

import discord4j.common.util.Snowflake
import pet.pupgrammer.b4tea4t.database.UserLevelData
import pet.pupgrammer.b4tea4t.extensions.toSnowflake

data class UserLevel(
    val guildId: Snowflake,
    val memberId: Snowflake,
    val xp: Float,
) {
    constructor(data: UserLevelData): this (
        guildId = data.guildId.toSnowflake(),
        memberId = data.memberId.toSnowflake(),
        xp = data.xp,
    )
}