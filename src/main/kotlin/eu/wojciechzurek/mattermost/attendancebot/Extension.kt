package eu.wojciechzurek.mattermost.attendancebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


fun <T> loggerFor(clazz: Class<T>): Logger = LoggerFactory.getLogger(clazz)
fun Long.toStringDateTime(): String = SimpleDateFormat("HH:mm:ss yyyy-MM-dd").format(Date(this))
fun Long.toStringDate(): String = SimpleDateFormat("yyyy-MM-dd").format(Date(this))
fun Long.toTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("UTC"))
    val instant = Instant.ofEpochMilli(this)
    return formatter.format(instant)
}

fun LocalDateTime.toStringDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")
    return this.format(formatter)
}