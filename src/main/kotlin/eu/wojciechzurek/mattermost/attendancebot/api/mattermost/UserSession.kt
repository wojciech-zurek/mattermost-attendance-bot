package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserSession(

        val id: String,

        @JsonProperty("user_id")
        val userId: String,

        @JsonProperty("create_at")
        val createAt: Long,

        @JsonProperty("device_id")
        val deviceId: String,

        @JsonProperty("expires_at")
        val expiresAt: Long,

        @JsonProperty("is_oauth")
        val isOauth: Boolean,

        @JsonProperty("last_activity_at")
        val lastActivityAt: Long,

        val props: Props? = null,

        val roles: String,

        val token: String
)