package eu.wojciechzurek.mattermost.attendancebot.domain

enum class UserMMStatus(val desc: String) {
    ONLINE("online"), AWAY("away"), DND("dnd"), OFFLINE("offline"), UNKNOWN("unknown")
}