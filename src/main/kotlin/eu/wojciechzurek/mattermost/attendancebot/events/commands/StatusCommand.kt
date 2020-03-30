package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Component
class StatusCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!status"

    override fun getHelp(): String = " !status - information about your work status"

    override fun onEvent(event: Event, message: String) = getUserInfo(event)

    private fun milli(dateTime: LocalDateTime): Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun getUserInfo(event: Event) {
        val userId = event.data.post!!.userId!!
        userRepository
                .findById(userId).zipWith(attendanceRepository.findLatestByMMUserId(userId))
                .map {
                    val message = when (it.t1.workStatus) {
                        WorkStatus.ONLINE -> {
                            "Online time: ${(System.currentTimeMillis() - milli(it.t1.workStatusUpdateDate)).toTime()}\n" +
                                    "Work start time: ${it.t2.signInDate.toStringDateTime()}\n" +
                                    "Today total away time: ${it.t2.awayTime.toTime()}\n"

                        }
                        WorkStatus.AWAY -> {
                            val away = System.currentTimeMillis() - milli(it.t1.workStatusUpdateDate)
                            "Away time: ${away.toTime()}\n" +
                                    "Today total away time: ${(it.t2.awayTime + away).toTime()}\n" +
                                    "Work start time: ${it.t2.signInDate.toStringDateTime()}\n"

                        }
                        WorkStatus.OFFLINE -> "\n"
                    }

                    EphemeralPost(it.t1.id,
                            Post(
                                    //  userId = it.data.post!!.userId,
                                    channelId = event.data.post.channelId,
                                    message = "${event.data.senderName}\n" +
                                            "Work status: ${it.t1.workStatus} (${it.t1.workStatusUpdateDate.toStringDateTime()})\n"
                                            + message
                            )
                    )
                }
                .map { mattermostService.ephemeralPost(it) }
                .subscribe()
    }
}