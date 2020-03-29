package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime
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
        val signOutDate: Long = 0L,

        val workTime: Int = 0,
        val absenceTime: Int = 0,

        val status: AttendanceStatus = AttendanceStatus.IN_WORK
)
