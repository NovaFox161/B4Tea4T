package pet.pupgrammer.b4tea4t.interaction

import discord4j.core.event.domain.interaction.InteractionCreateEvent


interface InteractionHandler<T : InteractionCreateEvent> {
    val ids: Array<String>
    val ephemeral: Boolean

    suspend fun shouldDefer(event: T): Boolean = true

    suspend fun handle(event: T)
}