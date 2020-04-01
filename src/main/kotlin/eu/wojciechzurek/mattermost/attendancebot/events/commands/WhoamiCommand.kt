package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component

@Component
class WhoamiCommand(
        private val userRepository: UserRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.whoami"

    override fun getHelp(): String = "- information about your account"

    override fun getCommandType(): CommandType = CommandType.INFO

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
                                    message = "User name: ${event.data.senderName}\n" +
                                            "Id: ${it.t1.id}\n" +
                                            "Email: ${it.t1.email}\n" +
                                            "Roles: ${it.t1.roles}\n" +
                                            "Account create at: ${it.t1.createAt.toStringDateTime()}\n" +
                                            "Work status: ${it.t2.workStatus} (${it.t2.workStatusUpdateDate.toStringDateTime()})\n"
                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}