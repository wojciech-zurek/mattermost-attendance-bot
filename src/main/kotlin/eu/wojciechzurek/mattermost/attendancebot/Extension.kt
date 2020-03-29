package eu.wojciechzurek.mattermost.attendancebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


fun <T> loggerFor(clazz: Class<T>): Logger = LoggerFactory.getLogger(clazz)
fun Long.toStringDateTime(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(this))
fun Long.toStringDate(): String = SimpleDateFormat("yyyy-MM-dd").format(Date(this))
