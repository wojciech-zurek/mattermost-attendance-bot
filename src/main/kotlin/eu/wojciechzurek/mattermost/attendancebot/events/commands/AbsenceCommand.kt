package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AbsencesRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.stream.Collectors

@Component
class AbsenceCommand(
        private val messageSource: MessageSource,
        private val absencesRepository: AbsencesRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.absence"

    override fun getHelp(): String = "[number] - show absences for working days. Default 10 days."

    override fun getCommandType(): CommandType = CommandType.STATS

    override fun onEvent(event: Event, message: String) = last(event, message)

    private fun last(event: Event, message: String) {

        val userId = event.data.post!!.userId!!

        val limit = if (message.isBlank()) 10 else message.toIntOrNull() ?: 10

        absencesRepository
                .findTopByMMUserId(userId, limit)
                .map {
                    messageSource.getMessage("theme.absence.row", arrayOf(
                            it.awayTime.toStringDateTime(),
                            it.onlineTime?.toStringDateTime() ?: "",
                            it.onlineTime?.let { t -> Duration.between(it.awayTime, t).seconds.toTime() } ?: "",
                            it.reason
                    ), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.absence", arrayOf(it), Locale.getDefault()) }
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