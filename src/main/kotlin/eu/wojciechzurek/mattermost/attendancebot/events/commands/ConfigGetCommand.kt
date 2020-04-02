package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.CommandType
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import eu.wojciechzurek.mattermost.attendancebot.toStringDateTime
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class ConfigGetCommand : AccessCommandSubscriber() {
    private val logger = loggerFor(this.javaClass)

    override fun getName(): String = "command.config.get"

    override fun getHelp(): String = "[keyname] - show configuration. Optional key/partial key name."

    override fun getCommandType(): CommandType = CommandType.CONFIG

    override fun onEvent(event: Event, message: String) = get(event, message)

    private fun get(event: Event, message: String) {

        val userId = event.data.post!!.userId!!

        val keys = message.split(" ")

        mattermostService
                .user(userId)
                .filter { it.roles.contains("system_admin") }
                .flatMapMany { (if (message.isBlank()) configService.findAll() else configService.findByPartialKey("%$message%")) }
                .sort { config, config2 -> String.CASE_INSENSITIVE_ORDER.compare(config.key, config2.key) }
                .map {
                    messageSource.getMessage("theme.config.row", arrayOf(
                            it.key,
                            it.value,
                            it.updateDate.toStringDateTime(),
                            it.userName
                    ), Locale.getDefault())
                }
                .collect(Collectors.joining())
                .map { messageSource.getMessage("theme.config", arrayOf(it), Locale.getDefault()) }
                .map {
                    EphemeralPost(
                            userId, Post(
                            channelId = event.data.post.channelId,
                            message = it
                    ))
                }
                .subscribe { mattermostService.ephemeralPost(it) }
    }
}