package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component

@Component
class WhoamiCommand : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!whoami"

    override fun getHelp(): String = " !whoami - information about your account"

    override fun onEvent(event: Event, message: String) = getUserInfo(event)

    private fun getUserInfo(event: Event) {
        mattermostService
                .user(event.data.post!!.userId!!)
                .map {
                    EphemeralPost(it.id,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "${event.data.senderName}\n" +
                                            "id: ${it.id}\n" +
                                            "email: ${it.email}\n" +
                                            "roles: ${it.roles}\n" +
                                            "create at: ${it.createAt.toStringDateTime()}"
                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}