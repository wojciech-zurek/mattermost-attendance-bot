package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.milli
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import java.time.*

@Component
class StatusCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.status"

    override fun getHelp(): String = "[username] - information about work status. Optional user name."

    override fun getCommandType(): CommandType = CommandType.INFO

    override fun onEvent(event: Event, message: String) = getUserInfo(event, message)

    private fun getUserInfo(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        val userName: String = (if (message.isBlank()) event.data.senderName!! else message).removePrefix("@")

        val now = OffsetDateTime.now()

        mattermostService
                .userName(userName)
                .flatMap {
                    userRepository
                            .findById(it.id).zipWith(attendanceRepository.findLatestByMMUserId(it.id))
                }
                .map {
                    val workTimeInSec = configService.get("work.time.in.sec").toLong()
                    val fields = when (it.t1.workStatus) {
                        WorkStatus.ONLINE -> {
                            listOf(
                                    Field(true, "Online time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime()),
                                    Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                    Field(true, "Today total away time", it.t2.awayTime.toTime()),
                                    Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime())
                            )

                        }
                        WorkStatus.AWAY -> {
                            val away = Duration.between(it.t1.workStatusUpdateDate, now).seconds
                            listOf(
                                    Field(true, "Away time", away.toTime()),
                                    Field(true, "Away reason", it.t1.absenceReason),
                                    Field(true, "Today total away time", (it.t2.awayTime + away).toTime()),
                                    Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                    Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime())
                            )

                        }
                        WorkStatus.OFFLINE -> {
                            listOf(
                                    Field(true, "Offline time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime())
                            )
                        }
                        WorkStatus.UNKNOWN -> {
                            listOf(
                                    Field(true, "Unknown status time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime())
                            )
                        }
                    }

                    Attachment(
                            authorName = userName,
//                            authorIcon = mattermostService.getUserImageEndpoint(it.t1.id),
                            title = it.t1.workStatus.toString(),
                            text = it.t1.workStatusUpdateDate.toStringDateTime(),
                            color = it.t1.workStatus.color,
                            thumbUrl = mattermostService.getUserImageEndpoint(it.t1.id),
                            fields = fields,
                            footer = ""
                    )

                }.map {
                    EphemeralPost(userId,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "",
                                    props = Props(listOf(it))

                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}