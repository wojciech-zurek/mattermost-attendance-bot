package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.Attendance
import eu.wojciechzurek.mattermost.attendancebot.domain.User
import eu.wojciechzurek.mattermost.attendancebot.domain.UserMMStatus
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Component
class StartCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.start"

    override fun getHelp(): String = "- start your new working day. You can use this command only once per day."

    override fun getCommandType(): CommandType = CommandType.MAIN

    override fun onEvent(event: Event, message: String) = start(event)

    private fun start(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val channelName = event.data.channelName
        val channelDisplayName = event.data.channelDisplayName
        val now = OffsetDateTime.now()

        userRepository
                .findById(userId)
                .switchIfEmpty {
                    mattermostService
                            .user(userId)
                            .map {

                                User(it.id, UUID.randomUUID(), it.userName, it.email,
                                        channelId, channelName, channelDisplayName, UserMMStatus.UNKNOWN,
                                        WorkStatus.ONLINE, now, "", now, now)
                                        .setNew()
                            }
                            .flatMap { userRepository.save(it) }
                }
                .flatMap { user ->
                    attendanceRepository
                            .findByMMUserIdAndWorkDate(user.userId, LocalDate.now())
                            .map { att ->
                                val message = when (user.workStatus) {
                                    WorkStatus.ONLINE -> {
                                        "Sorry but you are ONLINE already :thinking: \n" +
                                                "Work start time: " + att.signInDate.toStringDateTime() + "\n" +
                                                "Remember to stop your work with !stop command.\n" +
                                                "Thanks :smiley: Have a nice day.\n"
                                    }
                                    WorkStatus.AWAY -> {
                                        "Sorry but you are AWAY right now :thinking: \n" +
                                                "Remember to resume your work with !back command.\n"
                                    }

                                    WorkStatus.OFFLINE -> {
                                        "Sorry but you are OFFLINE and after work :thinking: \n" +
                                                "Stay save at home :mask: \n" +
                                                "Last work stop time: " + att.signOutDate?.toStringDateTime() + "\n" +
                                                "See you next time.\n"
                                    }
                                    WorkStatus.UNKNOWN -> {
                                        "You status is... UNKNOWN :thinking: \n" +
                                                "Contact with system admin. .\n"
                                    }
                                }
                                Post(channelId = channelId, message = message)
                            }
                            .switchIfEmpty {

                                user.workStatus = WorkStatus.ONLINE
                                user.workStatusUpdateDate = now
                                user.absenceReason = ""
                                user.updateDate = now
                                user.setOld()

                                userRepository
                                        .save(user)
                                        .map { Attendance(null, UUID.randomUUID(), it.userId, signInDate = now) }
                                        .flatMap { att -> attendanceRepository.save(att) }
                                        .map { att ->
                                            val workTimeInSec = configService.get("work.time.in.sec").toLong()

                                            val fields = listOf(
                                                    Field(false, "${user.workStatus} time", Duration.between(user.workStatusUpdateDate, now).seconds.toTime()),
                                                    Field(true, "Today total AWAY time", att.awayTime.toTime()),
                                                    Field(true, "Today total ONLINE time", "0"),
                                                    Field(true, "Work start time", att.signInDate.toStringDateTime()),
                                                    Field(true, "Estimated work stop time", att.signInDate.plusSeconds(workTimeInSec + att.awayTime).toStringDateTime())
                                            )

                                            Attachment(
                                                    authorName = user.userName,
//                            authorIcon = mattermostService.getUserImageEndpoint(it.t1.id),
                                                    title = user.workStatus.toString(),
                                                    text = user.workStatusUpdateDate.toStringDateTime(),
                                                    color = user.workStatus.color,
                                                    thumbUrl = mattermostService.getUserImageEndpoint(user.userId),
                                                    fields = fields,
                                                    footer = ""
                                            )
                                        }.map {
                                            Post(
                                                    channelId = channelId,
                                                    message = "You are ONLINE right now :innocent: \n" +
                                                            "Remember to stop your work with !stop command.\n" +
                                                            "Thanks :smiley: Have a nice day.\n",
                                                    props = Props(listOf(it))
                                            )
                                        }

                            }
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }

    }
}