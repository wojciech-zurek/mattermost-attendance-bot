package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelInfo(
        val id: String,
        @JsonProperty("create_at")
        val createAt: Long,
        @JsonProperty("update_at")
        val updateAt: Long,
        @JsonProperty("delete_at")
        val deleteAt: Long,
        @JsonProperty("team_id")
        val teamId: String,
        val type: String,
        @JsonProperty("display_name")
        val displayName: String,
        val name: String,
        val header: String,
        val purpose: String,
        @JsonProperty("last_post_at")
        val lastPostAt: Long,
        @JsonProperty("total_msg_count")
        val totalMsgCount: Long,
        @JsonProperty("creator_id")
        val creatorId: String
)