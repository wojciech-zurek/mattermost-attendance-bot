package eu.wojciechzurek.mattermost.attendancebot.domain

import java.time.LocalDate

data class ReportByDate(
        val workDate: LocalDate,
        val userName: List<String>

)

data class ReportByUserName(
        val userName: String,
        val workDate: List<String>

)