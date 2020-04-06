package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserStatus(

        @JsonProperty("user_id")
        val userId: String,
        val status: String,
        val manual: Boolean? = null,
        @JsonProperty("last_activity_at")
        val lastActivityAt: Long? = null
)