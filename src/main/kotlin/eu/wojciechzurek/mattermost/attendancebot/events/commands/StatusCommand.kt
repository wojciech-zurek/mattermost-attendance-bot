package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
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
                    val extraMessage = when (it.t1.workStatus) {
                        WorkStatus.ONLINE -> {
                            "Online time: ${Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime()}\n" +
                                    "Work start time: ${it.t2.signInDate.toStringDateTime()}\n" +
                                    "Today total away time: ${it.t2.awayTime.toTime()}\n" +
                                    "Estimated work stop time: ${it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime()}\n"

                        }
                        WorkStatus.AWAY -> {
                            val away = Duration.between(it.t1.workStatusUpdateDate, now).seconds
                            "Away reason: ${it.t1.absenceReason}\n" +
                                    "Away time: ${away.toTime()}\n" +
                                    "Today total away time: ${(it.t2.awayTime + away).toTime()}\n" +
                                    "Work start time: ${it.t2.signInDate.toStringDateTime()}\n" +
                                    "Estimated work stop time: ${it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime + away).toStringDateTime()}\n"

                        }
                        WorkStatus.OFFLINE -> "Offline time: ${Duration.between(it.t1.workStatusUpdateDate, now).seconds.toTime()}\n"
                    }

                    EphemeralPost(userId,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "User name: $userName\n" +
                                            "Work status: ${it.t1.workStatus} (${it.t1.workStatusUpdateDate.toStringDateTime()})\n"
                                            + extraMessage
                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}