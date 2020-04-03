package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class SessionCommand : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.session"

    override fun getHelp(): String = "[username] - session information about user"

    override fun getCommandType(): CommandType = CommandType.USER_MANAGEMENT

    override fun onEvent(event: Event, message: String) = getUserInfo(event, message.removePrefix("@"))

    override fun onApplicationEvent(event: ApplicationReadyEvent) {

    }

    private fun getUserInfo(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .user(userId)
                .filter { it.roles.contains("system_admin") }
                .flatMap { mattermostService.userName(message) }
                .flatMapMany { mattermostService.userSessions(it.id) }
                .sort { userSession, userSession2 -> userSession2.lastActivityAt.compareTo(userSession.lastActivityAt) }
                .map {
                    messageSource.getMessage("theme.session.row", arrayOf(
                            it.lastActivityAt.toStringDateTime(),
                            it.createAt.toStringDateTime(),
                            it.expiresAt.toStringDateTime(),
                            it.props?.platform,
                            it.props?.os,
                            it.props?.browser,

                            "props"
                    ), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.session", arrayOf(it), Locale.getDefault()) }
                .map {
                    EphemeralPost(userId, Post(
                            channelId = event.data.post.channelId,
                            message = it
                    ))
                }.map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}