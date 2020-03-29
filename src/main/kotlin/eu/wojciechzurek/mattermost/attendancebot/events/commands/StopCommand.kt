package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toWorkTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate

@Component
class StopCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : CommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    private val workTimeInMillis = 28800000

    override fun getPrefix(): String = "!stop"

    override fun getHelp(): String = " !stop - stop your working day. You can use this command only once per day."

    override fun onEvent(event: Event, message: String) = stop(event)

    private fun stop(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val date = LocalDate.now()

        userRepository
                .findById(userId)
                .filter { it.workStatus != WorkStatus.OFFLINE }
                .map { it.copy(workStatus = WorkStatus.OFFLINE) }
                .flatMap { userRepository.save(it) }
                .flatMap { attendanceRepository.findByMMUserIdAndWorkDate(it.userId, date) }
                .map {
                    val now = System.currentTimeMillis()
                    it.copy(
                            signOutDate = now,
                            workTime = now - it.signInDate
                    )
                }
                .flatMap { attendanceRepository.save(it) }
                .map {
                    Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "Work stop time: " + it.signOutDate.toStringDateTime() + "\n" +
                                    "Today work time : " + it.workTime.toWorkTime() + "\n" +
                                    "Thanks :smiley: You are after work right now . Have a nice day :innocent:\n"
                    )
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "You are not in work right now :thinking: \n" +
                                    "Start your work with !start command.\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }

    }
}