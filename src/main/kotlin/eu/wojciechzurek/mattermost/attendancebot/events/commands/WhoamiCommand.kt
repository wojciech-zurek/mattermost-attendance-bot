package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component

@Component
class WhoamiCommand(
        private val userRepository: UserRepository
) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!whoami"

    override fun getHelp(): String = " !whoami - information about your account"

    override fun onEvent(event: Event, message: String) = getUserInfo(event)

    private fun getUserInfo(event: Event) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .user(userId)
                .zipWith(userRepository.findById(userId))
                .map {
                    EphemeralPost(it.t1.id,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "${event.data.senderName}\n" +
                                            "id: ${it.t1.id}\n" +
                                            "email: ${it.t1.email}\n" +
                                            "roles: ${it.t1.roles}\n" +
                                            "account create at: ${it.t1.createAt.toStringDateTime()}\n" +
                                            "work status: ${it.t2.workStatus} (${it.t2.workStatusUpdateDate.toStringDateTime()})\n"
                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}