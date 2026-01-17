package pet.pupgrammer.b4tea4t.database

import org.springframework.data.relational.core.mapping.Table

@Table("user_levels")
data class UserLevelData(
    val guildId: Long,
    val memberId: Long,
    val xp: Float,
)