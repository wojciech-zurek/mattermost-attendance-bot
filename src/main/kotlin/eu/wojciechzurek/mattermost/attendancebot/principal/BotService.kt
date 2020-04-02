package eu.wojciechzurek.mattermost.attendancebot.principal

import eu.wojciechzurek.mattermost.attendancebot.events.CommandType

interface BotService {

    fun set(bot: Bot)
    fun get(): Bot

    fun getHelp(): Map<String, String>
    fun getCommands(): Map<CommandType, Set<String>>

    fun setHelp(command: String, message: String)
    fun setCommand(commandType: CommandType, command: String)
}