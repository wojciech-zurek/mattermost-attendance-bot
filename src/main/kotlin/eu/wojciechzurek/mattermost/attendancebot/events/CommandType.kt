package eu.wojciechzurek.mattermost.attendancebot.events

enum class CommandType(val descKey: String) {

    MAIN("commands.main"),
    STATS("commands.stats"),
    INFO("commands.info"),
    USER_MANAGEMENT("commands.user.management"),
    CONFIG("commands.config"),
    OTHER("commands.other")
}