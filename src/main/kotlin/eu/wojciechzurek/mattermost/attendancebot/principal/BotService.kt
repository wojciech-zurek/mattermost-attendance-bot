package eu.wojciechzurek.mattermost.attendancebot.principal

interface BotService {

    fun set(bot: Bot)
    fun get(): Bot

    fun setHelp(message: String)
    fun getHelp(): Set<String>
}