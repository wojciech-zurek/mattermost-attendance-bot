package eu.wojciechzurek.mattermost.attendancebot.principal

import eu.wojciechzurek.mattermost.attendancebot.events.CommandType

interface BotService {

    fun set(bot: Bot)
    fun get(): Bot

    fun setHelp(commandType: CommandType, message: String)
    fun getHelp(): Map<CommandType, Set<String>>
}