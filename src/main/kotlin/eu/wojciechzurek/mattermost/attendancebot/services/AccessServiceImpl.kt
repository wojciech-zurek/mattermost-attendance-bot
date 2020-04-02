package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.AccessValidationResult
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.events.AccessType
import org.springframework.stereotype.Service

@Service
class AccessServiceImpl(private val configService: ConfigService) : AccessService {

    private val allowUserKey = ".allow.user"
    private val allowChannelKey = ".allow.channel"
    private val blockUserKey = ".block.user"
    private val blockChannelKey = ".block.channel"

    override fun checkAccess(commandName: String, event: Event): AccessValidationResult {
        val userId = event.data.post!!.userId!!
        val userName = event.data.senderName?.removePrefix("@")!!
        val channelId = event.data.post.channelId
        val channelName = event.data.channelName!!

        val allowUser = configService.get(commandName + allowUserKey)

        val validAllowUser = if (allowUser.isNotBlank()) {
            allowUser.contains(userId) || allowUser.contains(userName)
        } else {
            true
        }

        if (!validAllowUser) return AccessValidationResult(userId, channelId, AccessType.NOT_ALLOWED_USER)

        val allowChannel = configService.get(commandName + allowChannelKey)

        val validAllowChannel = if (allowChannel.isNotBlank()) {
            allowChannel.contains(channelId) || allowChannel.contains(channelName)
        } else {
            true
        }

        if (!validAllowChannel) return AccessValidationResult(userId, channelId, AccessType.NOT_ALLOWED_CHANNEL)

        val blockUser = configService.get(commandName + blockUserKey)

        val validBlockUser = if (blockUser.isBlank()) {
            true
        } else {
            !blockUser.contains(userId) && !blockUser.contains(userName)
        }

        if (!validBlockUser) return AccessValidationResult(userId, channelId, AccessType.BLOCK_USER)

        val blockChannel = configService.get(commandName + blockChannelKey)

        val validBlockChannel = if (blockChannel.isBlank()) {
            true
        } else {
            !blockChannel.contains(channelId) && !blockChannel.contains(channelName)
        }

        if (!validBlockChannel) return AccessValidationResult(userId, channelId, AccessType.BLOCK_CHANNEL)

        return AccessValidationResult(userId, channelId, AccessType.ALLOWED)
    }
}