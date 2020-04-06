package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.Absence
import eu.wojciechzurek.mattermost.attendancebot.domain.UserMMStatus
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AbsencesRepository
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
import java.util.*

@Component
class AwayCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository,
        private val absencesRepository: AbsencesRepository

) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.away"

    override fun getHelp(): String = "[reason] - away from computer/home. Optional reason."

    override fun getCommandType(): CommandType = CommandType.MAIN

    override fun onEvent(event: Event, message: String) = away(event, message)

    private fun away(event: Event, message: String) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId

        val now = OffsetDateTime.now()
        val date = LocalDate.now()

        userRepository
                .findById(userId)
                .filter { it.workStatus == WorkStatus.ONLINE }
                .map {
                    it.copy(
                            workStatus = WorkStatus.AWAY,
                            workStatusUpdateDate = now,
                            absenceReason = message,
                            updateDate = now
                    )
                }
                .flatMap { userRepository.save(it).zipWith(attendanceRepository.findLatestByMMUserId(it.userId)) }
                .flatMap {
                    val absence = Absence(
                            null,
                            UUID.randomUUID(),
                            it.t2.id!!,
                            it.t2.userId,
                            message,
                            now
                    )
                    absencesRepository.save(absence).flatMap { _ ->
                        mattermostService.userStatus(UserStatus(it.t1.userId, UserMMStatus.AWAY.desc))
                    }.map { _ ->
                        val away = Duration.between(it.t1.workStatusUpdateDate, now).seconds
                        val workTimeInSec = configService.get("work.time.in.sec").toLong()

                        val onlineTime = Duration.between(it.t2.signInDate, now).seconds - it.t2.awayTime

                        val fields = listOf(
                                Field(true, "${it.t1.workStatus} time", away.toTime()),
                                Field(true, "${it.t1.workStatus} reason", it.t1.absenceReason),
                                Field(true, "Today total AWAY time", (it.t2.awayTime + away).toTime()),
                                Field(true, "Today total ONLINE time", onlineTime.toTime()),
                                Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime())
                        )

                        Attachment(
                                authorName = it.t1.userName,
//                            authorIcon = mattermostService.getUserImageEndpoint(it.t1.id),
                                title = it.t1.workStatus.toString(),
                                text = it.t1.workStatusUpdateDate.toStringDateTime(),
                                color = it.t1.workStatus.color,
                                thumbUrl = mattermostService.getUserImageEndpoint(it.t1.id),
                                fields = fields,
                                footer = ""
                        )
                    }
                }
                .map {
                    Post(
                            channelId = channelId,
                            message = "You are AWAY right now :smiling_imp: \n" +
                                    "Remember to resume your work with !back command.\n" +
                                    "Thanks :smiley: See you soon as possible.\n",
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