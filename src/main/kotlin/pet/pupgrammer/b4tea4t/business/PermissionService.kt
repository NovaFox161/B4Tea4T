package pet.pupgrammer.b4tea4t.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.RoleData
import discord4j.rest.util.PermissionSet
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import pet.pupgrammer.b4tea4t.config.Config
import java.util.function.Predicate

@Component
class PermissionService(
    private val beanFactory: BeanFactory
) {
    private val discordClient
        get() = beanFactory.getBean<DiscordClient>()

    suspend fun hasModRole(guildId: Snowflake, memberId: Snowflake): Boolean {
        val memberData = discordClient.getMemberById(guildId, memberId).data.awaitSingle()

        return memberData.roles().map(Snowflake::of).contains(Snowflake.of(Config.MOD_ROLE.getLong()))
    }

    suspend fun hasPermissions(guildId: Snowflake, memberId: Snowflake, pred: Predicate<PermissionSet>): Boolean {
        val guildData = discordClient.getGuildById(guildId).data.awaitSingle()

        // Owner has full permissions, always
        if (guildData.ownerId().asLong() == memberId.asLong()) return true

        val memberData = discordClient.getMemberById(guildId, memberId).data.awaitSingle()

        val computedPermissions = PermissionSet.of(
            guildData.roles()
                .filter { memberData.roles().contains(it.id()) }
                .map(RoleData::permissions)
                .reduceOrNull { acc, lng -> acc or lng } ?: 0L
        )

        return pred.test(computedPermissions)
    }
}