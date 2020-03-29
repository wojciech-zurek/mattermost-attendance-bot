package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelMember(
        @JsonProperty("channel_id")
        val channelId: String,
        @JsonProperty("user_id")
        val userId: String,
        val roles: String,
        @JsonProperty("msg_count")
        val msgCount: Int,
        @JsonProperty("last_viewed_at")
        val lastViewedAt: Long
)