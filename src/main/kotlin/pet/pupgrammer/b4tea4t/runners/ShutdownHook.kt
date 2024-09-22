package pet.pupgrammer.b4tea4t.runners

import discord4j.core.GatewayDiscordClient
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.logger.LOGGER
import pet.pupgrammer.b4tea4t.utils.GlobalValues

@Component
class ShutdownHook(private val discordClient: GatewayDiscordClient) {
    @PreDestroy
    fun onShutdown() {
        LOGGER.info(GlobalValues.STATUS, "Shutting down shard")

        discordClient.logout().subscribe()
    }
}