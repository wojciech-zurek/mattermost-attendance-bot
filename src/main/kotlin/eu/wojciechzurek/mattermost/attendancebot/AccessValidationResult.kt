package eu.wojciechzurek.mattermost.attendancebot

import eu.wojciechzurek.mattermost.attendancebot.events.AccessType

data class AccessValidationResult(
        val userId: String,
        val channelId: String,
        val accessType: AccessType
)