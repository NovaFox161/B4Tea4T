package pet.pupgrammer.b4tea4t.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.ComponentService
import pet.pupgrammer.b4tea4t.business.WelcomeMessageService
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage

@Component
class ConfigCommand(
    private val welcomeMessageService: WelcomeMessageService,
    private val componentService: ComponentService,
): SlashCommand {
    override val name = "config"
    override val hasSubcommands = true
    override val ephemeral = true

    override suspend fun shouldDefer(event: ChatInputInteractionEvent): Boolean {
        return when (event.options[0].name) {
            "welcome-message" -> {
                when (event.options[0].options[0].name) {
                    "create", "edit" -> false
                    else -> true
                }
            }
            else -> true
        }
    }

    override suspend fun handle(event: ChatInputInteractionEvent) {
        when (event.options[0].name) {
            "nick" -> nickname(event)
            "welcome-message" -> {
                when (event.options[0].options[0].name) {
                    "create" -> createWelcomeMessage(event)
                    "edit" -> editWelcomeMessage(event)
                    "enable" -> enableWelcomeMessage(event)
                    "channel" -> channelWelcomeMessage(event)
                    "delete" -> deleteWelcomeMessage(event)
                    else -> throw IllegalArgumentException("Unknown subcommand")
                }
            }
            else -> throw IllegalArgumentException("Unknown subcommand")
        }
    }

    private suspend fun nickname(event: ChatInputInteractionEvent) {
        val newName = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        event.client.getSelfMember(event.interaction.guildId.get()).flatMap {
            it.edit().withNicknameOrNull(newName)
        }.awaitSingleOrNull()

        event.createFollowup("Success")
            .withEphemeral(ephemeral)
            .awaitSingleOrNull()

    }

    private suspend fun createWelcomeMessage(event: ChatInputInteractionEvent) {
        val channelId = event.options[0].options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .orElse(event.interaction.channelId)
        val enabled = event.options[0].options[0].getOption("enabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        val existingWelcomeMessage = welcomeMessageService.getWelcomeMessage(event.interaction.guildId.get())
        val modifiedWelcomeMessage = existingWelcomeMessage?.copy(channelId = channelId, enabled = enabled) ?: WelcomeMessage(
            guildId = event.interaction.guildId.get(),
            enabled = enabled,
            channelId = channelId,
            messageContent = null,
            embedDescription = null,
        )
        welcomeMessageService.upsertWelcomeMessage(modifiedWelcomeMessage)

        event.presentModal()
            .withCustomId("edit-welcome-message-modal")
            .withTitle("Creating Welcome Message")
            .withComponents(*componentService.getEditWelcomeMessageComponents(existingWelcomeMessage))
            .awaitSingleOrNull()
    }

    private suspend fun editWelcomeMessage(event: ChatInputInteractionEvent) {
        val existingWelcomeMessage = welcomeMessageService.getWelcomeMessage(event.interaction.guildId.get())

        if (existingWelcomeMessage == null) {
            event.reply("Please create a welcome message first with `/config welcome-message create`")
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()
            return
        }

        event.presentModal()
            .withCustomId("edit-welcome-message-modal")
            .withTitle("Editing Welcome Message")
            .withComponents(*componentService.getEditWelcomeMessageComponents(existingWelcomeMessage))
            .awaitSingleOrNull()
    }

    private suspend fun enableWelcomeMessage(event: ChatInputInteractionEvent) {
        val enabled = event.options[0].options[0].getOption("enabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        val existingWelcomeMessage = welcomeMessageService.getWelcomeMessage(event.interaction.guildId.get())

        if (existingWelcomeMessage == null) {
            event.createFollowup("Please create a welcome message first with `/config welcome-message create`")
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()
            return
        }

        welcomeMessageService.upsertWelcomeMessage(existingWelcomeMessage.copy(enabled = enabled))

        event.createFollowup("Successfully toggled enablement of the welcome message")
            .withEphemeral(ephemeral)
            .awaitSingleOrNull()
    }

    private suspend fun channelWelcomeMessage(event: ChatInputInteractionEvent) {
        val channelId = event.options[0].options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .orElse(event.interaction.channelId)

        val existingWelcomeMessage = welcomeMessageService.getWelcomeMessage(event.interaction.guildId.get())

        if (existingWelcomeMessage == null) {
            event.createFollowup("Please create a welcome message first with `/config welcome-message create`")
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()
            return
        }

        welcomeMessageService.upsertWelcomeMessage(existingWelcomeMessage.copy(channelId = channelId))

        event.createFollowup("Successfully set the announcement message channel to <#${channelId.asString()}>")
            .withEphemeral(ephemeral)
            .awaitSingleOrNull()
    }

    private suspend fun deleteWelcomeMessage(event: ChatInputInteractionEvent) {
        welcomeMessageService.deleteWelcomeMessage(event.interaction.guildId.get())

        event.createFollowup("Successfully deleted the welcome message. Use `/config welcome-message create` to re-enable this feature.")
            .withEphemeral(ephemeral)
            .awaitSingleOrNull()
    }
}