package pet.pupgrammer.b4tea4t.business

import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.`object`.component.TextInput
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage

@Component
class ComponentService {
    fun getEditWelcomeMessageComponents(existingMessage: WelcomeMessage?): Array<LayoutComponent> {
        val messageContentInput = TextInput.paragraph(
            "edit-welcome-message.message-content",
            "Message Content",
            0,
            2000
        ).placeholder("Thanks for joining %guild.name% %user.mention%!")
            .prefilled(existingMessage?.messageContent ?: "")
            .required(false)
        val embedDescriptionInput = TextInput.paragraph(
            "edit-welcome-message.embed-description",
            "Embed Description",
            0,
            2000
        ).placeholder("Anything you'd like to add here, markdown formatting is supported...")
            .prefilled(existingMessage?.embedDescription ?: "")
            .required(false)

        return arrayOf(ActionRow.of(messageContentInput), ActionRow.of(embedDescriptionInput))
    }
}