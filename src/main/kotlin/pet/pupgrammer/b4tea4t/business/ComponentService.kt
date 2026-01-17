package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.component.*
import discord4j.core.`object`.emoji.Emoji
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage

@Component
class ComponentService(
    private val levelService: LevelService,
) {
    fun getEditWelcomeMessageComponents(existingMessage: WelcomeMessage?): Array<LayoutComponent> {
        val messageContentInput = TextInput.paragraph(
            "edit-welcome-message.message-content",
            0,
            2000
        ).placeholder("Thanks for joining %guild.name% %user.mention%!")
            .prefilled(existingMessage?.messageContent ?: "")
            .required(false)
        val embedDescriptionInput = TextInput.paragraph(
            "edit-welcome-message.embed-description",
            0,
            2000
        ).placeholder("Anything you'd like to add here, markdown formatting is supported...")
            .prefilled(existingMessage?.embedDescription ?: "")
            .required(false)


        return arrayOf(
            Label.of("Message Content", messageContentInput),
            Label.of("Embed Description", embedDescriptionInput)
        )
    }

    fun getEchoModalComponents(): Array<LayoutComponent> {
        val messageContentInput = TextInput.paragraph(
            "echo.message-content",
            0,
            2000,
        ).required(true)

        return arrayOf(Label.of("Message Content", messageContentInput))
    }

    suspend fun getLeaderboardPaginationComponents(guildId: Snowflake, currentPage: Int): Array<LayoutComponent> {
        val pageCount = levelService.getLeaderboardPageCount(guildId)


        val previousPageButton = Button.primary(
            "leaderboard-prev-$currentPage",
            Emoji.custom(Snowflake.of(1461946383199371417), "arrow_left", false),
        ).disabled(currentPage <= 0)
        val nextPageButton = Button.primary(
            "leaderboard-next-$currentPage",
            Emoji.custom(Snowflake.of(1461946383748694067), "arrow_right", false),
        ).disabled(currentPage >= pageCount - 1)
        val refreshButton = Button.secondary(
            "leaderboard-refresh-$currentPage",
            Emoji.custom(Snowflake.of(1461946385334276280), "refresh", false)
        )

        return arrayOf(ActionRow.of(previousPageButton, nextPageButton, refreshButton))
    }
}