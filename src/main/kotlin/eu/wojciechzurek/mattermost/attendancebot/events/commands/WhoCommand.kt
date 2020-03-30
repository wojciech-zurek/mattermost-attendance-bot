package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WhoCommand(private val userRepository: UserRepository) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!who "

    override fun getHelp(): String = " !who [username] - information about user"

    override fun onEvent(event: Event, message: String) = getUserInfo(event, message.removePrefix("@"))

    private fun getUserInfo(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .userName(message)
                .flatMap {
                    Mono.just(it).zipWith(userRepository.findById(it.id))
                }
                .map {
                    EphemeralPost(userId, Post(
                            channelId = event.data.post.channelId,
                            message = "${event.data.senderName}\n" +
                                    "user name: ${it.t1.userName}\n" +
                                    "email: ${it.t1.email}\n" +
                                    "roles: ${it.t1.roles}\n" +
                                    "create at: ${it.t1.createAt.toStringDateTime()}\n" +
                                    "work status: ${it.t2.workStatus} (${it.t2.workStatusUpdateDate.toStringDateTime()})\n"
                    ))
                }.map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}