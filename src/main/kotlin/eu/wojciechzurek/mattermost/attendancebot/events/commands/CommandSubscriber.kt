package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.ConfigReloadedEvent
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.events.PostedEventSubscriber
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.services.ConfigService
import eu.wojciechzurek.mattermost.attendancebot.services.MattermostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.MessageSource

abstract class CommandSubscriber : PostedEventSubscriber(), ApplicationListener<ApplicationReadyEvent> {

    private val logger = loggerFor(this.javaClass)

    @Autowired
    protected lateinit var mattermostService: MattermostService

    @Autowired
    protected lateinit var configService: ConfigService

    @Autowired
    protected lateinit var messageSource: MessageSource

    private val prefix = ".prefix"

    abstract fun getName(): String

    abstract fun getHelp(): String

    abstract fun getCommandType(): CommandType

    abstract fun onEvent(event: Event, message: String)

    private fun getPrefix(): String = getName() + prefix

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        botService.setCommand(getCommandType(), getPrefix())
        botService.setHelp(getPrefix(), getHelp())
    }

    override fun filter(event: Event): Boolean {
        return (event.data.post?.message?.trimStart()?.startsWith(configService.get(getPrefix()))
                ?: false) && super.filter(event)
    }

    override fun onEvent(event: Event) {
        logger.info("Command: {}", getName())
        onEvent(event, event.data.post?.message?.removePrefix(configService.get(getPrefix()))?.trim()!!)
    }

    override fun onError(event: Event, e: Exception) {
        val post = Post(
                //  userId = it.data.post!!.userId,
                channelId = event.data.post?.channelId!!,
                message = "${event.data.senderName}\n" +
                        "command error!\n" +
                        "exception:\n" +
                        "```\n" +
                        "${e.message}!\n" +
                        "```\n" +
                        "usage: ${configService.get(getPrefix()) + " " + getHelp()}"
        )

        mattermostService.post(post)
    }
}