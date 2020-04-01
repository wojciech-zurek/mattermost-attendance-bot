package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors

@Component
class AttendanceCommand(
        private val messageSource: MessageSource,
        private val userRepository: UserRepository
) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!attendance"

    override fun getHelp(): String = "!attendance - show all employees status in current channel"

    override fun getCommandType(): CommandType = CommandType.STATS

    override fun onEvent(event: Event, message: String) = attendance(event)

    private fun attendance(event: Event) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .channelMembers(event.data.post.channelId)
                .flatMap { mattermostService.user(it.userId).zipWith(userRepository.findById(it.userId)) }
                .map {
                    messageSource.getMessage("theme.channel-attendance.row", arrayOf(mattermostService.getUserImageEndpoint(it.t1.id), it.t2.userName, it.t1.email,
                            it.t2.workStatus.toString() + " " + it.t2.getFormattedAbsenceReason(), it.t2.workStatusUpdateDate.toStringDateTime()), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.channel-attendance", arrayOf(it), Locale.getDefault()) }
                .map {
                    EphemeralPost(
                            userId, Post(
                            channelId = event.data.post.channelId,
                            message = "${event.data.senderName}\n" +
                                    it
                    ))
                }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}