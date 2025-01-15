package pet.pupgrammer.b4tea4t.database

data class WelcomeMessageData(
    val guildId: Long,
    val enabled: Boolean,
    val channelId: Long,
    val messageContent: String?,
    val embedDescription: String?,
)
