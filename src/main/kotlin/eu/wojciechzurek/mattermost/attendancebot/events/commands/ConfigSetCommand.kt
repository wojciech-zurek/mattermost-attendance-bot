package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ConfigSetCommand : AccessCommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.config.set"

    override fun getHelp(): String = "[keyname] [value] - set new configuration value"

    override fun getCommandType(): CommandType = CommandType.CONFIG

    override fun onEvent(event: Event, message: String) = get(event, message)

    private fun get(event: Event, message: String) {

        val userId = event.data.post!!.userId!!
        val userName = event.data.senderName!!

        val keys = message.split(" ")
        val key = keys[0]
        val value = keys.subList(1, keys.count()).joinToString("") { it.trim() }

        mattermostService
                .user(userId)
                .filter { it.roles.contains("system_admin") }
                .flatMap { configService.findById(key) }
                .map { it.copy(value = value, userName = userName, updateDate = OffsetDateTime.now()) }
                .flatMap { configService.save(it) }
                .map {
                    EphemeralPost(
                            userId, Post(
                            channelId = event.data.post.channelId,
                            message = "Config updated:\n" +
                                    "$key = $value"
                    ))
                }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}