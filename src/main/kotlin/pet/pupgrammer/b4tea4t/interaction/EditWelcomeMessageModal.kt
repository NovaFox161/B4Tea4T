package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.TextInput
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.WelcomeMessageService
import pet.pupgrammer.b4tea4t.extensions.embedDescriptionSafe
import pet.pupgrammer.b4tea4t.extensions.messageContentSafe
import kotlin.jvm.optionals.getOrNull

@Component
class EditWelcomeMessageModal(
    private val welcomeMessageService: WelcomeMessageService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("edit-welcome-message-modal")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent) {
        val guildId = event.interaction.guildId.get()
        val inputs = event.getComponents(TextInput::class.java)

        val messageContent = inputs.first { it.customId == "edit-welcome-message.message-content" }.value.getOrNull()
        val embedDescription = inputs.first { it.customId == "edit-welcome-message.embed-description" }.value.getOrNull()

        val existingWelcomeMessage = welcomeMessageService.getWelcomeMessage(guildId)

        // This way we don't worry about a user creating welcome messages in the wrong channel implicitly
        if (existingWelcomeMessage == null) {
            event.createFollowup("Failed to save changes, please make sure a welcome message already exists with `/config welcome-message create`")
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()
            return
        }

        val modifiedWelcomeMessage = existingWelcomeMessage.copy(
            messageContent = messageContent?.messageContentSafe(),
            embedDescription = embedDescription?.embedDescriptionSafe(),
        )

        welcomeMessageService.upsertWelcomeMessage(modifiedWelcomeMessage)

        event.createFollowup("Successfully set welcome message content")
            .withEphemeral(ephemeral)
            .awaitSingleOrNull()
    }
}