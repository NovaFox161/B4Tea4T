package pet.pupgrammer.b4tea4t.database

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("message_records")
data class MessageRecordData(
    val messageId: Long,
    val guildId: Long,
    val memberId: Long,
    val channelId: Long,
    val wordCount: Int,
    val dayBucket: LocalDate
)