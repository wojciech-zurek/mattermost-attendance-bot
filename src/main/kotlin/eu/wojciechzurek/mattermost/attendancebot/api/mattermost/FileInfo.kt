package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileInfo(
        @JsonProperty("client_ids")
        val clientIds: List<String>,

        @JsonProperty("file_infos")
        val fileInfos: List<Info>
)

data class Info(
        val id: String,
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("post_id")
        val postId: String?,
        @JsonProperty("create_at")
        val createAt: String,
        val name: String,
        val extension: String,
        val size: Long,
        @JsonProperty("mime_type")
        val mimeType: String,
        val width: Int?,
        val height: Int?,
        @JsonProperty("has_preview_image")
        val hasPreviewImage: Boolean?
)