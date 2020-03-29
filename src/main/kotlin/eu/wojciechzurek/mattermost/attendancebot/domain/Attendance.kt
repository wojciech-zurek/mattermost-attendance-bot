package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*

@Table("attendance")
data class Attendance(
        @Id
        val id: Long?,

        val publicId: UUID,

        @Column("mm_user_id")
        val userId: String,

        val workDate: LocalDate = LocalDate.now(),

        val signInDate: Long = System.currentTimeMillis(),
        var signOutDate: Long = 0L,

        var workTime: Long = 0L,
        val absenceTime: Long = 0L
)
