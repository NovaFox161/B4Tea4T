package pet.pupgrammer.b4tea4t

import discord4j.common.util.Snowflake
import pet.pupgrammer.b4tea4t.cache.CacheRepository
import pet.pupgrammer.b4tea4t.`object`.MessageRecord
import pet.pupgrammer.b4tea4t.`object`.UserLevel
import pet.pupgrammer.b4tea4t.`object`.WelcomeMessage

// Cache
typealias WelcomeMessageCache = CacheRepository<Snowflake, WelcomeMessage>
typealias MessageRecordCache = CacheRepository<Snowflake, MessageRecord>
typealias UserLevelCache = CacheRepository<Snowflake, UserLevel>
typealias DaysActiveCache = CacheRepository<Snowflake, Long>
typealias LeveledUserCountCache = CacheRepository<Snowflake, Long>