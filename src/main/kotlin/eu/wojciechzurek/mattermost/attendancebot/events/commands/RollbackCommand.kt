package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime

@Component
class RollbackCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository
) : CommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.rollback"

    override fun getHelp(): String = "[username] - rollback user offline status to online"

    override fun getCommandType(): CommandType = CommandType.USER_MANAGEMENT

    override fun onEvent(event: Event, message: String) = rollback(event, message.removePrefix("@"))

    private fun rollback(event: Event, message: String) {

        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId

        val now = OffsetDateTime.now()

        mattermostService
                .user(userId)
                .filter { it.roles.contains("system_admin") }
                .flatMap { mattermostService.userName(message) }
                .flatMap {
                    userRepository.findById(it.id)
                }.filter {
                    it.workStatus == WorkStatus.OFFLINE
                }.map {
                    it.copy(
                            workStatus = WorkStatus.ONLINE,
                            workStatusUpdateDate = now,
                            absenceReason = "",
                            updateDate = now
                    )
                }
                .flatMap { userRepository.save(it) }
                .flatMap { attendanceRepository.findByMMUserIdAndWorkDate(it.userId, LocalDate.now()) }
                .map {
                    it.copy(
                            signOutDate = null,
                            workTime = 0L
                    )
                }
                .flatMap { attendanceRepository.save(it) }
                .map {
                    Post(
                            channelId = channelId,
                            message = "User status set to ONLINE\n")
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "Sorry but user status is not OFFLINE\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}