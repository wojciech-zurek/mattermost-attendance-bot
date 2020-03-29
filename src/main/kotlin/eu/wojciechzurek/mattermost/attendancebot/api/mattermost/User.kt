package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
        val id: String,
        @JsonProperty("username")
        val userName: String,
        @JsonProperty("first_name")
        val firstName: String?,
        @JsonProperty("last_name")
        val lastName: String?,
        @JsonProperty("nickname")
        val nickName: String?,
        @JsonProperty("email")
        val email: String?,
        @JsonProperty("roles")
        val roles: String,
        @JsonProperty("create_at")
        val createAt: Long
)