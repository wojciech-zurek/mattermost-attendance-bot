package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component

@Component
class WhoCommand : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!who "

    override fun getHelp(): String = " !who [username] - information about user"

    override fun onEvent(event: Event, message: String) = getUserInfo(event, message.removePrefix("@"))

    private fun getUserInfo(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .userName(message)
                .map {
                    EphemeralPost(userId, Post(
                            channelId = event.data.post.channelId,
                            message = "${event.data.senderName}\n" +
                                    "user name: ${it.userName}\n" +
                                    "email: ${it.email}\n" +
                                    "roles: ${it.roles}\n" +
                                    "create at: ${it.createAt.toStringDateTime()}"
                    ))
                }.map { mattermostService.ephemeralPost(it) } //// val data = Data(userId = event.data.post.userId, post = it) }
                .subscribe()
    }
}