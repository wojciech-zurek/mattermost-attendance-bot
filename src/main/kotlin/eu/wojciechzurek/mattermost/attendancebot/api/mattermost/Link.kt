package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Link(
        val link: String
)