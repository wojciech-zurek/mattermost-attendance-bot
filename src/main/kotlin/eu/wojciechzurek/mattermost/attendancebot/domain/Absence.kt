package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.*

@Table("absences")
data class Absence(
        @Id
        val id: Long?,

        val publicId: UUID,

        val attendanceId: Long,

        @Column("mm_user_id")
        val userId: String,

        val reason: String = "",

        val awayTime: OffsetDateTime,

        val awayType: StatusType = StatusType.MANUAL,

        var onlineTime: OffsetDateTime? = null,

        var onlineType: StatusType? = null
)
