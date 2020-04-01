package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("configs")
data class Config(
        @Id val key: String,
        val value: String,
        @Column("mm_user_name")
        val userName: String,
        val updateDate: OffsetDateTime = OffsetDateTime.now()
)