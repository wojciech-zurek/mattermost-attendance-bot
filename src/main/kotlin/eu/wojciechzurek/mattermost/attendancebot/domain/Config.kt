package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("configs")
data class Config(
        @Id val key: String,
        val value: String
)