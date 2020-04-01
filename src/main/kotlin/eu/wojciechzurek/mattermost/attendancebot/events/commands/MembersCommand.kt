package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors

@Component
class MembersCommand(
        private val messageSource: MessageSource
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.members"

    override fun getHelp(): String = "- show all members in current channel"

    override fun getCommandType(): CommandType = CommandType.STATS

    override fun onEvent(event: Event, message: String) = show(event)

    private fun show(event: Event) {
        val userId = event.data.post!!.userId!!
        mattermostService
                .channelMembers(event.data.post.channelId)
                .flatMap { Mono.just(it).zipWith(mattermostService.user(it.userId)) }
                .map {
                    messageSource.getMessage("theme.channel-members.row", arrayOf(mattermostService.getUserImageEndpoint(it.t1.userId), it.t2.userName, it.t2.email,
                            it.t2.roles, it.t1.msgCount, it.t1.lastViewedAt.toStringDateTime()), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.channel-members", arrayOf(it), Locale.getDefault()) }
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