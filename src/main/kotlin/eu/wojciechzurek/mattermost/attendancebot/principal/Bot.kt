package eu.wojciechzurek.mattermost.attendancebot.principal

data class Bot (
        val userId: String,
        val userName: String,
        val roles: String,
        val systemAdmin: Boolean
)