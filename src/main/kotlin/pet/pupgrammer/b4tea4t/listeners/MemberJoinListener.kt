package pet.pupgrammer.b4tea4t.listeners

import discord4j.core.event.domain.guild.MemberJoinEvent
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.business.WelcomeMessageService

@Component
class MemberJoinListener(
    private val welcomeMessageService: WelcomeMessageService,
): EventListener<MemberJoinEvent> {

    override suspend fun handle(event: MemberJoinEvent) {
        if (event.member.isBot) return

        welcomeMessageService.postWelcomeMessage(event.guildId, event.member)
    }
}