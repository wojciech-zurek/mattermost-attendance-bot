package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.repository.ReportRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.streams.toList

@Component
class ReportMonthlyCommand(
        private val reportRepository: ReportRepository
) : AccessCommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.report.monthly"

    override fun getHelp(): String = "[month].[year] - get report about all user"

    override fun getCommandType(): CommandType = CommandType.USER_MANAGEMENT

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
    }

    override fun onEvent(event: Event, message: String) = get(event, message)


    private fun get(event: Event, message: String) {

        val userId = event.data.post!!.userId!!
        val channelId = event.data.post.channelId
        val userName = event.data.senderName!!

        val now = LocalDate.now()
        val args = message.split(".")
        var month = now.monthValue
        var year = now.year
        if (args.size == 2) {
            month = args[0].toInt()
            year = args[1].toInt()
        }

        reportRepository
                .reportByUserName(month, year)
                .collectList()
                .map {

                    val map = mutableMapOf<String, MutableList<String>>()
                    map["Date"] = mutableListOf()

                    it.map { report ->
                        map["Date"]?.add(report.userName)

                        DayIterator(month, year).asSequence().forEach { day ->
                            if (report.workDate.contains(day.toString()))
                                map.getOrPut(day.toString(), { mutableListOf() }).add("Obecny")
                            else
                                map.getOrPut(day.toString(), { mutableListOf() }).add("Nieobecny")
                        }
                    }
                    map
                }.map { m ->
                    m.map {
                        it.key.plus(it.value.joinToString(",", ",", "\n"))
                    }.reduce { acc, s -> acc + s }
                }
                .flatMap { mattermostService.file(it.toByteArray(), "attendance_list_${month}_${year}.csv", channelId) }
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

class DayIterator(month: Int, year: Int) : Iterator<LocalDate> {

    private var min: LocalDate = LocalDate.of(year, month, 1)
    private val max: LocalDate = min.plusMonths(1)

    override fun hasNext(): Boolean {
        return min.isBefore(max)
    }

    override fun next(): LocalDate {
        val current = min
        min = min.plusDays(1)
        return current
    }
}