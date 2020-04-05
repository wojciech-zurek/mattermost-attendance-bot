package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.OffsetDateTime

@Component
class WhoisCommand(
        private val userRepository: UserRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.whois"

    override fun getHelp(): String = "@username - information about user"

    override fun getCommandType(): CommandType = CommandType.INFO

    override fun onEvent(event: Event, message: String) = getUserInfo(event, message.removePrefix("@"))

    private fun getUserInfo(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        val userName: String = (if (message.isBlank()) event.data.senderName!! else message).removePrefix("@")

        mattermostService
                .userName(userName)
                .flatMap {
                    Mono.just(it).zipWith(userRepository.findById(it.id))
                }
                .map {

                    val fields = mutableListOf(
                            Field(true, "${it.t2.workStatus} time", Duration.between(it.t2.workStatusUpdateDate, OffsetDateTime.now()).seconds.toTime()),
                            Field(true, "Email", it.t1.email),
                            Field(true, "Roles", it.t1.roles),
                            Field(true, "Account create at", it.t1.createAt.toStringDateTime())
                    )

                    if (userId == it.t1.id) {
                        fields.add(Field(true, "ID", it.t1.id))
                    }

                    Attachment(
                            authorName = it.t1.userName,
//                            authorLink = event.data.senderName,
//                            authorIcon = mattermostService.getUserImageEndpoint(it.t1.id),
                            title = it.t2.workStatus.toString(),
                            text = it.t2.workStatusUpdateDate.toStringDateTime(),
                            color = it.t2.workStatus.color,
                            thumbUrl = mattermostService.getUserImageEndpoint(it.t1.id),
                            fields = fields,
                            footer = ""
                    )
                }

                .map {
                    EphemeralPost(
                            userId,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "",
                                    props = Props(listOf(it))
                            )
                    )
                }.map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}