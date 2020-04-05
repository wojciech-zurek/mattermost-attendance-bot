package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Component
class StopCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.stop"

    override fun getHelp(): String = "- stop your working day. You can use this command only once per day."

    override fun getCommandType(): CommandType = CommandType.MAIN

    override fun onEvent(event: Event, message: String) = stop(event)

    private fun stop(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId

        val now = OffsetDateTime.now()

        userRepository
                .findById(userId)
                .filter { it.workStatus == WorkStatus.ONLINE }
                .map {

                    it.copy(
                            workStatus = WorkStatus.OFFLINE,
                            workStatusUpdateDate = now,
                            absenceReason = "",
                            updateDate = now
                    )
                }
                .flatMap { userRepository.save(it) }
                .flatMap { user ->
                    attendanceRepository.findLatestByMMUserId(user.userId)
                            .map { att ->
                                att.copy(
                                        signOutDate = now,
                                        workTime = Duration.between(att.signInDate, now).seconds
                                )
                            }.flatMap {
                                attendanceRepository.save(it)
                            }.map {
                                val fields = listOf(
                                        Field(false, "${user.workStatus} time", Duration.between(user.workStatusUpdateDate, now).seconds.toTime()),
                                        Field(true, "Total AWAY time", it.awayTime.toTime()),
                                        Field(true, "Total ONLINE time", (it.workTime - it.awayTime).toTime()),
                                        Field(true, "Work start time", it.signInDate.toStringDateTime())
                                )

                                Attachment(
                                        authorName = user.userName,
                                        title = user.workStatus.toString(),
                                        text = user.workStatusUpdateDate.toStringDateTime(),
                                        color = user.workStatus.color,
                                        thumbUrl = mattermostService.getUserImageEndpoint(user.userId),
                                        fields = fields,
                                        footer = ""
                                )
                            }
                }
                .map {
                    Post(
                            channelId = channelId,
                            message = "You are OFFLINE right now :sunglasses: \n" +
                                    "Thanks :smiley: You are after work. Have a nice day.\n",
                            props = Props(listOf(it))
                    )
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "Sorry but you are not ONLINE right now :thinking: \n" +
                                    "Start your work with !start or back to work with !back command.\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}