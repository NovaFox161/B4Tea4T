package pet.pupgrammer.b4tea4t.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.RestClient
import discord4j.store.api.mapping.MappingStoreService
import discord4j.store.api.service.StoreService
import discord4j.store.jdk.JdkStoreService
import discord4j.store.redis.RedisClusterStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import pet.pupgrammer.b4tea4t.listeners.EventListener
import pet.pupgrammer.b4tea4t.mapper.SnowflakeMapper
import reactor.kotlin.core.publisher.toFlux

@Configuration
class DiscordConfig {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        // Use d4j's object mapper
        return JacksonResources.create().objectMapper
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .registerModule(SnowflakeMapper())
    }

    @Bean
    fun discordGatewayClient(
        listeners: List<EventListener<*>>,
        stores: StoreService,
        strategy: ShardingStrategy,
    ): GatewayDiscordClient {
        return DiscordClientBuilder.create(Config.SECRET_BOT_TOKEN.getString())
            .build().gateway()
            .setEnabledIntents(getIntents())
            .setSharding(strategy)
            .setStore(Store.fromLayout(LegacyStoreLayout.of(stores)))
            .setInitialPresence { ClientPresence.online(ClientActivity.playing(Config.INITIAL_STATUS_MESSAGE.getString())) }
            .setMemberRequestFilter(MemberRequestFilter.none())
            .withEventDispatcher { dispatcher ->
                @Suppress("UNCHECKED_CAST")
                (listeners as Iterable<EventListener<Event>>).toFlux()
                    .flatMap {
                        dispatcher.on(it.genericType) { event -> mono { it.handle(event) } }
                    }
            }
            .login()
            .block()!!
    }

    @Bean
    fun discordRestClient(gatewayDiscordClient: GatewayDiscordClient): RestClient {
        return gatewayDiscordClient.restClient
    }

    @Bean
    fun discordStores(): StoreService {
        val useRedis = Config.CACHE_USE_REDIS.getBoolean()
        val redisHost = Config.REDIS_HOST.getString()
        val redisPort = Config.REDIS_PORT.getInt()
        val redisPassword = Config.REDIS_PASSWORD.getString().toCharArray()
        val isRedisCluster = Config.CACHE_REDIS_IS_CLUSTER.getBoolean()

        return if (useRedis) {
            val uriBuilder = RedisURI.Builder
                .redis(redisHost, redisPort)
            if (redisPassword.isNotEmpty()) uriBuilder.withPassword(redisPassword)

            val rss = if (isRedisCluster) {
                RedisClusterStoreService.Builder()
                    .redisClient(RedisClusterClient.create(uriBuilder.build()))
                    .build()
            } else {
                RedisStoreService.Builder()
                    .redisClient(RedisClient.create(uriBuilder.build()))
                    .build()
            }


            MappingStoreService.create()
                .setFallback(rss)
                .setFallback(JdkStoreService())
        } else JdkStoreService()
    }

    @Bean
    fun shardingStrategy(): ShardingStrategy {
        return ShardingStrategy.builder()
            .count(1)
            .indices(0)
            .build()
    }


    private fun getIntents() = IntentSet.of(
        Intent.GUILDS,
        Intent.GUILD_MESSAGES,
        Intent.GUILD_MESSAGE_REACTIONS,
        Intent.DIRECT_MESSAGES,
        Intent.DIRECT_MESSAGE_REACTIONS,
        Intent.GUILD_MEMBERS,
        Intent.GUILD_EMOJIS_AND_STICKERS,
        Intent.GUILD_VOICE_STATES,
    )
}