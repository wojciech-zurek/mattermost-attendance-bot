package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
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
) : CommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "command.prefix.stop"

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
                .flatMap { attendanceRepository.findByMMUserIdAndWorkDate(it.userId, LocalDate.now()) }
                .map {
                    it.copy(
                            signOutDate = now,
                            workTime = Duration.between(it.signInDate, now).seconds
                    )
                }
                .flatMap { attendanceRepository.save(it) }
                .map {
                    Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "You are OFFLINE right now :sunglasses: \n" +
                                    "Work stop time: " + it.signOutDate?.toStringDateTime() + "\n" +
                                    "Today work time : " + it.workTime.toTime() + "\n" +
                                    "Today away time : " + it.awayTime.toTime() + "\n" +
                                    "Thanks :smiley: You are after work. Have a nice day.\n"
                    )
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "Sorry but you are not ONLINE right now :thinking: \n" +
                                    "Start your work with !start or back to work with !online command.\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}