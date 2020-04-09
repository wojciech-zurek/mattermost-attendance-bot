package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.ReportByDate
import eu.wojciechzurek.mattermost.attendancebot.domain.ReportByUserName
import reactor.core.publisher.Flux
import java.time.LocalDate

interface ReportRepository {
    fun users(month: Int, year: Int): Flux<String>
    fun reportByDate(start: LocalDate, stop: LocalDate): Flux<ReportByDate>
    fun reportByUserName(month: Int, year: Int): Flux<ReportByUserName>
}