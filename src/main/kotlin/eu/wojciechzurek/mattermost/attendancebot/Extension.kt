package eu.wojciechzurek.mattermost.attendancebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


fun <T> loggerFor(clazz: Class<T>): Logger = LoggerFactory.getLogger(clazz)
fun Long.toStringDateTime(): String = SimpleDateFormat("HH:mm:ss yyyy-MM-dd").format(Date(this))
fun Long.toStringDate(): String = SimpleDateFormat("yyyy-MM-dd").format(Date(this))
fun Long.toTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("UTC"))
    val instant = Instant.ofEpochMilli(this * 1000)
    return formatter.format(instant)
}

fun LocalDate.toStringDate(): String = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
fun LocalDateTime.milli(): Long = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun LocalDateTime.toStringDateTime(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd"))

fun OffsetDateTime.toStringDateTime(): String = this.format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd"))