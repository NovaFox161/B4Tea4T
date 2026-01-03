package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.discordjson.json.MessageReferenceData
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component

@Component
class EchoService {
    suspend fun sendMessage(channel: GuildMessageChannel, content: String, replyTo: Snowflake? = null): Message {
        var messageMono = channel.createMessage(content)

        if (replyTo != null) messageMono = messageMono.withMessageReference(MessageReferenceData.builder()
            .messageId(replyTo.asLong()).build())

        return messageMono.awaitSingle()
    }
}