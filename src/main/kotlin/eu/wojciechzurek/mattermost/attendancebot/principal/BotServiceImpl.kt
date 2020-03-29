package eu.wojciechzurek.mattermost.attendancebot.principal

import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.stereotype.Service

@Service
class BotServiceImpl : BotService {

    private lateinit var bot: Bot
    private val logger = loggerFor(this.javaClass)

    private val help: MutableSet<String> = mutableSetOf<String>().toSortedSet()

    override fun set(bot: Bot) {
        logger.info("Setting new bot: {}", bot)
        this.bot = bot
    }

    override fun get(): Bot = bot

    override fun setHelp(message: String) {
        help.add(message)
    }

    override fun getHelp(): Set<String> = help
}