package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.StatusType
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

@Component
class BackCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository,
        private val absencesRepository: AbsencesRepository
) : AccessCommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.back"

    override fun getHelp(): String = "- set online status (back from away status)"

    override fun getCommandType(): CommandType = CommandType.MAIN

    override fun onEvent(event: Event, message: String) = back(event)

    private fun back(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val now = OffsetDateTime.now()

        userRepository
                .findById(userId)
                .filter { it.workStatus == WorkStatus.AWAY }
                .map {
                    it.copy(
                            workStatus = WorkStatus.ONLINE,
                            workStatusUpdateDate = now,
                            absenceReason = "",
                            updateDate = now
                    )
                }
                .flatMap { userRepository.save(it) }
                .flatMap { user ->
                    mattermostService.userStatus(UserStatus(user.userId, UserMMStatus.ONLINE.desc))
                            .flatMap {
                                attendanceRepository.findLatestByMMUserId(user.userId)
                            }
                            .flatMap { att ->
                                absencesRepository.findByAttendanceId(att.id!!).zipWith(Mono.just(att))
                            }
                            .flatMap {
                                it.t1.onlineTime = now
                                it.t1.onlineType = StatusType.MANUAL
                                val awayTime = Duration.between(it.t1.awayTime, it.t1.onlineTime!!).seconds
                                it.t2.awayTime = it.t2.awayTime + awayTime

                                absencesRepository.save(it.t1).zipWith(attendanceRepository.save(it.t2))
                            }
                            .map {
                                val workTimeInSec = configService.get("work.time.in.sec").toLong()
                                val onlineTime = Duration.between(it.t2.signInDate, now).seconds - it.t2.awayTime

                                val fields = listOf(
                                        Field(false, "${user.workStatus} time", Duration.between(user.workStatusUpdateDate, now).seconds.toTime()),
                                        Field(true, "Today total AWAY time", it.t2.awayTime.toTime()),
                                        Field(true, "Today total ONLINE time", onlineTime.toTime()),
                                        Field(true, "Work start time", it.t2.signInDate.toStringDateTime()),
                                        Field(true, "Estimated work stop time", it.t2.signInDate.plusSeconds(workTimeInSec + it.t2.awayTime).toStringDateTime())
                                )

                                Attachment(
                                        authorName = user.userName,
                                        title = user.workStatus.toString(),
                                        text = user.workStatusUpdateDate.toStringDateTime(),
                                        color = user.workStatus.color,
                                        thumbUrl = mattermostService.getUserImageEndpoint(user.userId),
                                        fields = fields,
                                        footer = ""
                                )
                            }
                }
                .map {
                    Post(
                            channelId = channelId,
                            message = "You are ONLINE right now :innocent: \n" +
                                    "Thanks :smiley: Go back to work.\n",
                            props = Props(listOf(it))
                    )
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "Sorry but you are not AWAY right now :thinking: \n" +
                                    "To start away just use !away command, but you must be at work.\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
    }

}