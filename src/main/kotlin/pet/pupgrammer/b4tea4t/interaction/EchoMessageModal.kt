package pet.pupgrammer.b4tea4t.interaction

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.TextInput
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.rest.util.Permission
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.EchoService
import pet.pupgrammer.b4tea4t.business.PermissionService

@Component
class EchoMessageModal(
    private val permissionService: PermissionService,
    private val echoService: EchoService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("echo")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent) {
        val guildId = event.interaction.guildId.get()
        val userId = event.interaction.user.id

        val inputs = event.getComponents(TextInput::class.java)
        val messageContent = inputs.first { it.customId == "echo.message-content" }.value.get()

        // get channel and message reference ID from custom ID - Snowflakes would need to be over twice as long for this to be a problem
        val splitId = event.customId.split(".") // #echo.{channel_id}.{message_reference?}
        val channelId = Snowflake.of(splitId[1])
        val messageReference = if (splitId.size > 2) Snowflake.of(splitId[2]) else null
        val channel = event.client.getChannelById(channelId)
            .ofType(GuildMessageChannel::class.java)
            .awaitSingle()

        // Validate perms
        val hasPermission = permissionService.hasModRole(guildId, userId) || permissionService.hasPermissions(guildId, userId) {
            it.contains(Permission.MANAGE_MESSAGES)
        }
        if (!hasPermission) {
            event.createFollowup("You do not have permission to use this echo feature")
            return
        }

        // Send
        val message = echoService.sendMessage(channel, messageContent, messageReference)
        event.createFollowup("<https://discord.com/channels/${guildId.asString()}/${message.channelId.asString()}/${message.id.asString()}>")
            .withEphemeral(true)
            .awaitSingleOrNull()
    }
}