package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.domain.StatusType
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AbsencesRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class OnlineCommand(
        private val userRepository: UserRepository,
        private val attendanceRepository: AttendanceRepository,
        private val absencesRepository: AbsencesRepository
) : CommandSubscriber() {

    private val logger = loggerFor(this.javaClass)

    override fun getPrefix(): String = "!online"

    override fun getHelp(): String = " !online - set online status (back from away status)"

    override fun onEvent(event: Event, message: String) = online(event)

    private fun online(event: Event) {
        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val now = LocalDateTime.now()

        userRepository
                .findById(userId)
                .filter { it.workStatus == WorkStatus.AWAY }
                .map {
                    it.copy(
                            workStatus = WorkStatus.ONLINE,
                            workStatusUpdateDate = now,
                            updateDate = now
                    )
                }
                .flatMap { userRepository.save(it) }
                .flatMap {
                    attendanceRepository
                            .findByMMUserIdAndWorkDate(it.userId, LocalDate.now())
                            .flatMap { att ->
                                absencesRepository.findByAttendanceId(att.id!!).zipWith(Mono.just(att))
                            }
                }
                .map {
                    it.t1.onlineTime = System.currentTimeMillis()
                    it.t1.onlineType = StatusType.MANUAL
                    val awayTime = (it.t1.onlineTime!! - it.t1.awayTime)
                    it.t2.awayTime = it.t2.awayTime + awayTime
                    it
                }
                .flatMap {
                    absencesRepository.save(it.t1).zipWith(attendanceRepository.save(it.t2))
                }
                .map {
                    Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "You are ONLINE right now :innocent: \n" +
                                    "Away time: " + (it.t1.onlineTime!! - it.t1.awayTime).toTime() + "\n" +
                                    "Today total away time: " + (it.t2.awayTime).toTime() + "\n" +
                                    "Thanks :smiley: Go back to work.\n"
                    )
                }
                .switchIfEmpty {
                    Mono.just(Post(
                            channelId = channelId,
                            message = "${event.data.senderName}\n" +
                                    "Sorry but you are not AWAY right now :thinking: \n" +
                                    "To start away just use !away command, but you must be at work.\n"
                    ))
                }
                .map { EphemeralPost(userId, it) }
                .subscribe { mattermostService.ephemeralPost(it) }
    }

}