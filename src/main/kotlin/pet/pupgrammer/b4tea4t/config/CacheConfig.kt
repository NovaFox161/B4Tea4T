package pet.pupgrammer.b4tea4t.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import pet.pupgrammer.b4tea4t.*
import pet.pupgrammer.b4tea4t.cache.JdkCacheRepository
import pet.pupgrammer.b4tea4t.cache.RedisStringCacheRepository
import pet.pupgrammer.b4tea4t.extensions.asMinutes

@Configuration
class CacheConfig {
    private val welcomeMessageTtl = Config.CACHE_TTL_WELCOME_MESSAGE.getLong().asMinutes()
    private val messageRecordTtl = Config.CACHE_TTL_MESSAGE_RECORD_MINUTES.getLong().asMinutes()
    private val userLevelTtl = Config.CACHE_TTL_USER_LEVEL_MINUTES.getLong().asMinutes()
    private val daysActiveTtl = Config.CACHE_TTL_DAYS_ACTIVE_MINUTES.getLong().asMinutes()
    private val leveledUserCountTtl = Config.CACHE_TTL_LEVELED_USER_MINUTES.getLong().asMinutes()

    // Redis caching
    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun welcomeMessageRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): WelcomeMessageCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "WelcomeMessages", welcomeMessageTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun messageRecordRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): MessageRecordCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "MessageRecords", messageRecordTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun userLevelRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): UserLevelCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "UserLevels", userLevelTtl)

    @Bean(name = ["daysActiveCache"])
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun daysActiveRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): DaysActiveCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "DaysActive", daysActiveTtl)

    @Bean(name = ["leveledUserCountCache"])
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun leveledUserCountRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): LeveledUserCountCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "LeveledUserCounts", leveledUserCountTtl)


    // In-memory fallback caching
    @Bean
    fun welcomeMessageFallbackCache(): WelcomeMessageCache = JdkCacheRepository(welcomeMessageTtl)

    @Bean
    fun messageRecordCache(): MessageRecordCache = JdkCacheRepository(messageRecordTtl)

    @Bean
    fun userLevelCache(): UserLevelCache = JdkCacheRepository(userLevelTtl)

    @Bean(name = ["daysActiveCache"])
    @ConditionalOnProperty("bot.cache.redis", havingValue = "false")
    fun daysActiveCache(): DaysActiveCache = JdkCacheRepository(daysActiveTtl)

    @Bean(name = ["leveledUserCountCache"])
    @ConditionalOnProperty("bot.cache.redis", havingValue = "false")
    fun leveledUserCountCache(): LeveledUserCountCache = JdkCacheRepository(leveledUserCountTtl)
}