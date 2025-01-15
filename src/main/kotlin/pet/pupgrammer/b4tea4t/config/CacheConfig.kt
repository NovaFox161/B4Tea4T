package pet.pupgrammer.b4tea4t.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import pet.pupgrammer.b4tea4t.WelcomeMessageCache
import pet.pupgrammer.b4tea4t.cache.JdkCacheRepository
import pet.pupgrammer.b4tea4t.cache.RedisStringCacheRepository
import pet.pupgrammer.b4tea4t.extensions.asMinutes

@Configuration
class CacheConfig {
    private val welcomeMessageTtl = Config.CACHE_TTL_WELCOME_MESSAGE.getLong().asMinutes()

    // Redis caching
    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun welcomeMessageRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): WelcomeMessageCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "WelcomeMessages", welcomeMessageTtl)


    // In-memory fallback caching
    @Bean
    fun welcomeMessageFallbackCache(): WelcomeMessageCache = JdkCacheRepository(welcomeMessageTtl)
}