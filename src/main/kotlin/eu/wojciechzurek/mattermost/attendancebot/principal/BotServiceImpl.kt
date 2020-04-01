package eu.wojciechzurek.mattermost.attendancebot.principal

import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*

@Service
class BotServiceImpl : BotService, InitializingBean {

    private lateinit var bot: Bot
    private val logger = loggerFor(this.javaClass)

    private val help: MutableMap<CommandType, MutableSet<String>> = TreeMap<CommandType, MutableSet<String>>().toMutableMap()

    override fun afterPropertiesSet() {
        CommandType.values().map {
            help[it] = mutableSetOf<String>().toSortedSet()
        }
    }

    override fun set(bot: Bot) {
        logger.info("Setting new bot: {}", bot)
        this.bot = bot
    }

    override fun get(): Bot = bot

    override fun setHelp(commandType: CommandType, message: String) {
        help[commandType]?.add(message)
    }

    override fun getHelp(): Map<CommandType, Set<String>> = help
}