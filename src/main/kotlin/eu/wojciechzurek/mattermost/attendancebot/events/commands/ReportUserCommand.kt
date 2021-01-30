package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.AbsencesRepository
import eu.wojciechzurek.mattermost.attendancebot.repository.AttendanceRepository
import eu.wojciechzurek.mattermost.attendancebot.toStringDate
import eu.wojciechzurek.mattermost.attendancebot.toTime
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.stream.Collectors
import kotlin.streams.toList

@Component
class ReportUserCommand(
    private val attendanceRepository: AttendanceRepository,
    private val absencesRepository: AbsencesRepository
) : AccessCommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.report.user"

    override fun getHelp(): String = "@username [month].[year] - get report about user"

    override fun getCommandType(): CommandType = CommandType.USER_MANAGEMENT

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
    }

    override fun onEvent(event: Event, message: String) = get(event, message)


    private fun get(event: Event, message: String) {

        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        // val userName = event.data.senderName!!

        val args = message.split(" ")//@username 01.2021
        val userName = args[0].removePrefix("@")

        val now = LocalDate.now()
        var month = now.monthValue
        var year = now.year

        if (args.size == 2) {
            val date = args[1].split(".") //01.2021
            month = date[0].toInt()
            year = date[1].toInt()
        }

        mattermostService.userName(userName).flatMapMany { user ->
            DayIterator(month, year).toFlux().map {
                Pair(it, attendanceRepository.findByMMUserIdAndDate(it, user.id))
            }

        }.flatMap {
            val date = it.first.toStringDate()

            val attendance: Mono<String> = it.second.map { att ->
                val d = Duration.between(att.signInDate, att.signInDate.withHour(7).withMinute(0).withSecond(0))
                var early = 0L
                val diff = if (d.isNegative) {
                    "+${d.negated().seconds.toTime()}"
                } else {
                    early = d.seconds
                    "-${d.seconds.toTime()}"
                }

                val stopTime = att.signOutDate?.toTime() ?: ""

                val signOutDate = att.signOutDate ?: OffsetDateTime.now()
                val workTime = Duration.between(att.signInDate, signOutDate).seconds
                val totalOnlineTime = workTime - att.awayTime

                val earlyWorkTime = workTime - early
                val totalEarlyWorkTime = totalOnlineTime - early

                val a =
                    ";${att.signInDate.toTime()};${diff};${stopTime};${workTime.toTime()};${totalOnlineTime.toTime()};${earlyWorkTime.toTime()};${totalEarlyWorkTime.toTime()};${att.awayTime.toTime()}"
                Pair(att.id, a)
            }.flatMapMany { p ->
                val avs: Mono<String> = absencesRepository.findAllByAttendanceId(p.first!!).map { a ->
                    val onlineTime = a.onlineTime?.toTime() ?: ""
                    val totalOnlineTime =
                        Duration.between(a.awayTime, a.onlineTime ?: OffsetDateTime.now()).seconds.toTime()
                    "\n;;;;;;;;;${a.awayTime.toTime()};${onlineTime};${totalOnlineTime};${a.reason}"
                }.collect(Collectors.joining())
                    .switchIfEmpty(Mono.just(""))
                Mono.just(p.second).mergeWith(avs)
            }
                .collect(Collectors.joining())
                .switchIfEmpty { Mono.just("") }

            Mono.just(date).zipWith(attendance)

        }.map {
            "${it.t1}${it.t2}"
        }.collectSortedList { t, t2 -> t.compareTo(t2) }
            .map {
                val head =
                    "Date;Start time;Start time diff 7:00;Stop time;Total online time;Total online time minus total away;Total online time minus diff;Total online time minus away minus diff;Total away time;Away time;Online/Back time;Away time in sec;Reason\n"
                val body = it.joinToString("\n")
                "$head$body"
            }
            .flatMap { mattermostService.file(it.toByteArray(), "report_${userName}_${month}_${year}.csv", channelId) }
            .zipWith(mattermostService.directMessageChannel(listOf(botService.get().userId, userId)))
            .map {
                Post(
                    userId = userId,
                    channelId = it.t2.id,
                    message = userName,
                    fileIds = it.t1.fileInfos.stream().map { file -> file.id }.toList()
                )
            }
            .subscribe {
                mattermostService.post(it)
            }
    }
}