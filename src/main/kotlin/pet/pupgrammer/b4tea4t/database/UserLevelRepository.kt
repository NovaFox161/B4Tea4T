package pet.pupgrammer.b4tea4t.database

import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserLevelRepository : R2dbcRepository<UserLevelData, Long> {
    fun deleteByGuildIdAndMemberId(guildId: Long, memberId: Long): Mono<Void>

    fun existsByGuildIdAndMemberId(guildId: Long, memberId: Long): Mono<Boolean>

    fun findByGuildIdAndMemberId(guildId: Long, memberId: Long): Mono<UserLevelData>

    fun findAllByGuildIdOrderByXpDesc(guildId: Long, pageable: Pageable): Flux<UserLevelData>

    fun findAllByGuildId(guildId: Long): Flux<UserLevelData>

    fun countByGuildId(guildId: Long): Mono<Long>

    @Query("""
        WITH rank AS (
            SELECT
                ul.member_id,
                RANK() OVER(ORDER BY xp DESC)  AS rn 
            FROM user_levels AS ul
            WHERE guild_id = :guildId
            ORDER BY xp DESC
        )
        
        SELECT rn FROM rank WHERE member_id = :memberId;
    """)
    fun calculateRankByGuildIdAndMemberId(guildId: Long, memberId: Long): Mono<Long>

    @Query("""
        UPDATE user_levels
        SET xp = :xp
        WHERE guild_id = :guildId 
            AND member_id = :memberId
    """)
    fun updateByGuildIdAndMemberId(
        guildId: Long,
        memberId: Long,
        xp: Float,
    ): Mono<Int>
}