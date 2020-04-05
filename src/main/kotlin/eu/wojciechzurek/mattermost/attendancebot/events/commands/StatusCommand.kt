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

    override fun getHelp(): String = "@username - information about work status. Optional user name."

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

                            val onlineTime = Duration.between(it.t2.signInDate, now).seconds - it.t2.awayTime

                            listOf(
                                    Field(false, "${it.t1.workStatus} time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime()),
                                    Field(true, "Today total AWAY time", it.t2.awayTime.toTime()),
                                    Field(true, "Today total ONLINE time", onlineTime.toTime()),
                                    Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                    Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime())
                            )
                        }
                        WorkStatus.AWAY -> {
                            val away = Duration.between(it.t1.workStatusUpdateDate, now).seconds
                            val totalAway = it.t2.awayTime + away
                            val onlineTime = Duration.between(it.t2.signInDate, now).seconds - totalAway
                            listOf(
                                    Field(true, "${it.t1.workStatus} time", away.toTime()),
                                    Field(true, "${it.t1.workStatus} reason", it.t1.absenceReason),
                                    Field(true, "Today total AWAY time", totalAway.toTime()),
                                    Field(true, "Today total ONLINE time", onlineTime.toTime()),
                                    Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                    Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + totalAway).toStringDateTime())
                            )
                        }
                        WorkStatus.OFFLINE -> {
                            listOf(
                                    Field(false, "${it.t1.workStatus} time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime()),
                                    Field(true, "Last total AWAY time", it.t2.awayTime.toTime()),
                                    Field(true, "Last total ONLINE time", (it.t2.workTime - it.t2.awayTime).toTime()),
                                    Field(true, "Last work start time", it.t2.signInDate.toStringDateTime())

                            )
                        }
                        WorkStatus.UNKNOWN -> {
                            listOf(
                                    Field(true, "${it.t1.workStatus} status time", Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime())
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