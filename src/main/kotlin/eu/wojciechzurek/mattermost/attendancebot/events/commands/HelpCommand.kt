package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.stereotype.Component

@Component
class HelpCommand : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!help"

    override fun getHelp(): String = " !help - this message"

    override fun onEvent(event: Event, message: String) = help(event)

    private fun help(event: Event) {

        val help = botService.getHelp().joinToString("\n")
        val userId = event.data.post!!.userId!!

        val post = Post(
                //  userId = it.data.post!!.userId,
                channelId = event.data.post.channelId,
                message = "${event.data.senderName}\n" +
                        "command list:\n" +
                        help
        )

        val ephemeralPost = EphemeralPost(userId, post)

        mattermostService.ephemeralPost(ephemeralPost)
    }
}