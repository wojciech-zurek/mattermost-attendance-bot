package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.AccessValidationResult
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.AccessType
import eu.wojciechzurek.mattermost.attendancebot.services.AccessService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import java.util.*

abstract class AccessCommandSubscriber : CommandSubscriber() {

    @Autowired
    lateinit var accessService: AccessService

    override fun filter(event: Event): Boolean {
        if (!super.filter(event)) return false

        val result = accessService.checkAccess(getName(), event)
        when (result.accessType) {
            AccessType.NOT_ALLOWED_USER -> onAccessError(result.userId, result.channelId, "commands.access.not.allowed.user")
            AccessType.BLOCK_USER -> onAccessError(result.userId, result.channelId, "commands.access.block.user")
            AccessType.NOT_ALLOWED_CHANNEL -> onAccessError(result.userId, result.channelId, "commands.access.not.allowed.channel")
            AccessType.BLOCK_CHANNEL -> onAccessError(result.userId, result.channelId, "commands.access.block.channel")
            AccessType.ALLOWED -> return true
        }
        return false
    }

    private fun onAccessError(userId: String, channelId: String, key: String) {

        val reason = messageSource.getMessage(key, null, Locale.getDefault())

        val post = Post(
                channelId = channelId,
                message = "Access denied! :rage: \n" +
                        "Reason: $reason\n"

        )

        val ephemeralPost = EphemeralPost(userId, post)
        mattermostService.ephemeralPost(ephemeralPost)
    }

}