package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*

@Component
class HelpCommand(private val messageSource: MessageSource) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "command.prefix.help"

    override fun getHelp(): String = "- this message"

    override fun getCommandType(): CommandType = CommandType.OTHER

    override fun onEvent(event: Event, message: String) = help(event)

    private fun help(event: Event) {

        val help = botService
                .getHelp()
                .map {
                    messageSource.getMessage(it.key.descKey, null, Locale.getDefault()) + ":\n" + it.value.joinToString("\n\t", "\t")
                }
                .joinToString("\n\n")

        val userId = event.data.post!!.userId!!

        val post = Post(
                //  userId = it.data.post!!.userId,
                channelId = event.data.post.channelId,
                message = "${event.data.senderName}\n\n" +
                        help
        )

        val ephemeralPost = EphemeralPost(userId, post)

        mattermostService.ephemeralPost(ephemeralPost)
    }
}