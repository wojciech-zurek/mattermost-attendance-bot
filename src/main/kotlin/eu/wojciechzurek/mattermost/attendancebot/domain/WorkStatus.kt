package eu.wojciechzurek.mattermost.attendancebot.domain

enum class WorkStatus(val color: String) {
    UNKNOWN("#9E9E9E"),
    ONLINE("#00C853"),
    AWAY("#FF6D00"),
    OFFLINE("#d50000")
}