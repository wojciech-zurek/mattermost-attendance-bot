package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(
        val event: String,
        val data: Data,
        val broadcast: BroadCast,
        val seq: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Data(
        @JsonProperty("parent_id")
        val parentId: String? = null,
        @JsonProperty("user_id")
        val userId: String? = null,
        @JsonProperty("channel_display_name")
        val channelDisplayName: String? = null,
        @JsonProperty("channel_name")
        val channelName: String? = null,
        @JsonProperty("channel_type")
        val channelType: String? = null,
        @JsonProperty("sender_name")
        val senderName: String? = null,
        @JsonDeserialize(using = PostDeserializer::class)
        val post: Post? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BroadCast(
        @JsonProperty("user_id")
        val userId: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EphemeralPost (
        @JsonProperty("user_id")
        val userId: String,
        val post: Post
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Post(
        val id: String? = null,
        @JsonProperty("create_at")
        val createAt: Long? = null,
        @JsonProperty("user_id")
        val userId: String? = null,
        @JsonProperty("channel_id")
        val channelId: String,
        val message: String,
        @JsonProperty("file_ids")
        val fileIds: List<String>? = null,
        val metadata: Metadata? = null,
        val props: Props? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Metadata(
        val embeds: List<Embed>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Embed(
        val type: String? = null,
        val url: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Props(
        val attachments: List<Attachment>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attachment(
        val fallback: String? = null,
        val color: String? = null,
        val pretext: String? = null,
        val text: String? = null,
        @JsonProperty("author_name")
        val authorName: String? = null,
        @JsonProperty("author_icon")
        val authorIcon: String? = null,
        @JsonProperty("author_link")
        val authorLink: String? = null,
        val title: String? = null,
        @JsonProperty("title_link")
        val titleLink: String? = null,
        @JsonProperty("image_url")
        val imageUrl: String? = null,
        @JsonProperty("thumb_url")
        val thumbUrl: String? = null,
        val footer: String? = null,
        val fields: List<Field>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Field(
        val short: Boolean? = null,
        val title: String? = null,
        val value: String? = null
)