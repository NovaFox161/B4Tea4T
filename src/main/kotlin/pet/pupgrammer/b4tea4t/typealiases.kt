package pet.pupgrammer.b4tea4t

import discord4j.common.util.Snowflake
import pet.pupgrammer.b4tea4t.cache.CacheRepository
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage

// Cache
typealias WelcomeMessageCache = CacheRepository<Snowflake, WelcomeMessage>