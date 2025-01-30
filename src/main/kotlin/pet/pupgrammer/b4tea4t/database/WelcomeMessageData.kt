package pet.pupgrammer.b4tea4t.database

import org.springframework.data.relational.core.mapping.Table

@Table("welcome_messages")
data class WelcomeMessageData(
    val guildId: Long,
    val enabled: Boolean,
    val channelId: Long,
    val messageContent: String?,
    val embedDescription: String?,
)
