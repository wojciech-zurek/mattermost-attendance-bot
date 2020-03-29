package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.Attendance
import eu.wojciechzurek.mattermost.attendancebot.domain.AttendanceStatus
import eu.wojciechzurek.mattermost.attendancebot.domain.User
import eu.wojciechzurek.mattermost.attendancebot.domain.UserStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class StartCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    private val workTimeInMillis = 28800000

    override fun getPrefix(): String = "!start"

    override fun getHelp(): String = " !start - start your new working day. You can use this command only once per day."

    override fun onEvent(event: Event, message: String) = now(event)

    private fun now(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val channelName = event.data.channelName
        val channelDisplayName = event.data.channelDisplayName
        val date = LocalDate.now()

        userRepository
                .findById(userId)
                .switchIfEmpty {
                    mattermostService
                            .user(userId)
                            .map {
                                User(
                                        it.id, UUID.randomUUID(), it.userName, it.email, channelId, channelName!!, channelDisplayName!!, UserStatus.UNKNOWN, LocalDateTime.now()
                                )
                                        .setNew(true)
                            }.flatMap { userRepository.save(it) }
                }
                .flatMap {
                    attendanceRepository
                            .findByMMUserIdAndWorkDate(it.userId, date)
                            .map { att ->
                                val message = when (att.status) {
                                    AttendanceStatus.IN_WORK -> {
                                        "${event.data.senderName}\n" +
                                                "You are in work already :sunglasses: \n" +
                                                "Work start time: " + att.signInDate.toStringDateTime() + "\n" +
                                                "Remember to stop your job with !stop command.\n"
                                    }
                                    AttendanceStatus.AFTER_WORK -> {
                                        "${event.data.senderName}\n" +
                                                "You are after work. Stay save at home :mask: \n" +
                                                "Work stop time: " + att.signOutDate.toStringDateTime() + "\n" +
                                                "See you next time.\n"
                                    }
                                }
                                Post(channelId = event.data.post.channelId, message = message)
                            }
                            .switchIfEmpty {
                                Mono
                                        .just(Attendance(null, UUID.randomUUID(), it.userId))
                                        .flatMap { att -> attendanceRepository.save(att) }
                                        .map {
                                            Post(
                                                    channelId = event.data.post.channelId,
                                                    message = "${event.data.senderName}\n" +
                                                            "Work start time: " + it.signInDate.toStringDateTime() + "\n" +
                                                            "You should end up after : " + (it.signInDate + workTimeInMillis).toStringDateTime() + "\n" +
                                                            "Remember to stop your job with !stop command.\n" +
                                                            "Thanks :smiley: You are at work right now . Have a nice day :innocent:\n"
                                            )
                                        }

                            }
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
//
//        val user = User(
//                null,
//
//        )


    }
}