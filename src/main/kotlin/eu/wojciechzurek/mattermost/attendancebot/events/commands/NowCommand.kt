package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component

@Component
class NowCommand : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.now"

    override fun getHelp(): String = "- current date time"

    override fun getCommandType(): CommandType = CommandType.OTHER

    override fun onEvent(event: Event, message: String) = now(event)

    private fun now(event: Event) {
        val userId = event.data.post!!.userId!!
        val now = System.currentTimeMillis()

        val post = Post(
                //  userId = it.data.post!!.userId,
                channelId = event.data.post.channelId,
                message = "Date: ${now.toStringDateTime()}\n" +
                        "Timestamp: $now"
        )

        val ephemeralPost = EphemeralPost(userId, post)
        mattermostService.ephemeralPost(ephemeralPost)
    }
}