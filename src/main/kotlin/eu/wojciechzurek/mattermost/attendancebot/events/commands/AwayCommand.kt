package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.Absence
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AbsencesRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
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
                .flatMap { userRepository.save(it) }
                .flatMap { attendanceRepository.findByMMUserIdAndWorkDate(it.userId, date) }
                .map {
                    Absence(
                            null,
                            UUID.randomUUID(),
                            it.id!!,
                            it.userId,
                            message,
                            now
                    )
                }
                .flatMap { absencesRepository.save(it) }
                .map {
                    Post(
                            channelId = channelId,
                            message = "You are AWAY right now :smiling_imp: \n" +
                                    "Away reason: ${it.reason}\n" +
                                    "Away start time: " + it.awayTime.toStringDateTime() + "\n" +
                                    "Remember to resume your work with !back command.\n" +
                                    "Thanks :smiley: See you soon as possible.\n"
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