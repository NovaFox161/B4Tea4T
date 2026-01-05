package pet.pupgrammer.b4tea4t.config

import java.io.FileReader
import java.util.*

enum class Config(private val key: String, private var value: Any? = null) {
    // Basic spring settings
    APP_NAME("spring.application.name"),

    // Redis cache settings
    REDIS_SSL("spring.data.redis.ssl.enabled", true),
    REDIS_SSL_TRUSTSTORE_FILE("bot.cache.redis.truststore.file", "ca-certificates.p12"),
    REDIS_SSL_TRUSTSTORE_PASSWORD("bot.cache.redis.truststore.password", ""),
    REDIS_HOST("spring.data.redis.host"),
    REDIS_PORT("spring.data.redis.port"),
    REDIS_DATABASE("spring.data.redis.database", -1),
    REDIS_USERNAME("spring.data.redis.username", ""),
    REDIS_PASSWORD("spring.data.redis.password", ""),
    CACHE_REDIS_IS_CLUSTER("redis.cluster", false),
    CACHE_USE_REDIS("bot.cache.redis", false),
    CACHE_PREFIX("bot.cache.prefix", "b4tea4t"),
    //CACHE_TTL_SETTINGS_MINUTES("bot.cache.ttl-minutes.settings", 60),
    CACHE_TTL_WELCOME_MESSAGE("bot.cache.ttl-minutes.welcome-message", 120),

    // Global bot timings

    // Bot secrets
    SECRET_BOT_TOKEN("bot.secret.token"),
    SECRET_CLIENT_SECRET("bot.secret.client-secret"),
    SECRET_WEBHOOK_DEBUG("bot.secret.debug-webhook"),
    SECRET_WEBHOOK_STATUS("bot.secret.status-webhook"),

    // Various URLs
    URL_BASE("bot.url.base"),
    URL_SUPPORT("bot.url.support", "https://discord.gg/2TFqyuy"),
    URL_INVITE("bot.url.invite"),


    // Everything else
    SHARD_COUNT("bot.sharding.count"),
    SHARD_INDEX("bot.sharding.index"),
    LOGGING_WEBHOOKS_USE("bot.logging.webhooks.use", false),
    LOGGING_WEBHOOKS_ALL_ERRORS("bot.logging.webhooks.all-errors", false),
    INITIAL_STATUS_MESSAGE("bot.initial-status-message"),
    AUDIT_LOG_CHANNEL("bot.audit-log.channel"),
    ANNOUNCEMENT_CHANNEL("bot.announcement.channel"),
    MOD_ROLE("bot.mod-role");

    companion object {
        fun init() {
            val props = Properties()
            props.load(FileReader("application.properties"))

            Config.entries.forEach { it.value = props.getProperty(it.key, it.value?.toString()) }
        }
    }


    fun getString() = value.toString()

    fun getInt() = getString().toInt()

    fun getLong() = getString().toLong()

    fun getBoolean() = getString().toBoolean()
}