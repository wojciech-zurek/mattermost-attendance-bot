package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDate
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class LastCommand(
        private val messageSource: MessageSource,
        private val attendanceRepository: AttendanceRepository
) : CommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "command.prefix.last"

    override fun getHelp(): String = "[number] - show stats for working days. Default 10 days."

    override fun getCommandType(): CommandType = CommandType.STATS

    override fun onEvent(event: Event, message: String) = last(event, message)

    private fun last(event: Event, message: String) {

        val userId = event.data.post!!.userId!!

        val limit = if (message.isBlank()) 10 else message.toIntOrNull() ?: 10

        attendanceRepository
                .findTopByMMUserId(userId, limit)
                .map {
                    messageSource.getMessage("theme.last.row", arrayOf(
                            it.workDate.toStringDate(),
                            it.signInDate.toStringDateTime(),
                            it.signOutDate?.toStringDateTime() ?: "",
                            it.workTime.toTime(),
                            it.awayTime.toTime()
                    ), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.last", arrayOf(it), Locale.getDefault()) }
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