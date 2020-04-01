package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.EphemeralPost
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post

abstract class AccessCommandSubscriber : CommandSubscriber() {

    private val allowUserKey = ".allow.user"
    private val allowChannelKey = ".allow.channel"
    private val blockUserKey = ".block.user"
    private val blockChannelKey = ".block.channel"

    override fun filter(event: Event): Boolean {
        return super.filter(event) && checkAccess(event)
    }

    private fun checkAccess(event: Event): Boolean {
        val userId = event.data.post!!.userId!!
        val userName = event.data.senderName?.removePrefix("@")!!
        val channelId = event.data.post.channelId
        val channelName = event.data.channelName!!

        val allowUser = configService.get(getName() + allowUserKey)

        val validAllowUser = if (allowUser.isNotBlank()) {
            allowUser.contains(userId) || allowUser.contains(userName)
        } else {
            true
        }

        val allowChannel = configService.get(getName() + allowChannelKey)

        val validAllowChannel = if (allowChannel.isNotBlank()) {
            allowChannel.contains(channelId) || allowChannel.contains(channelName)
        } else {
            true
        }

        val blockUser = configService.get(getName() + blockUserKey)

        val validBlockUser = if (blockUser.isBlank()) {
            true
        } else {
            !blockUser.contains(userId) && !blockUser.contains(userName)
        }

        val blockChannel = configService.get(getName() + blockChannelKey)

        val validBlockChannel = if (blockChannel.isBlank()) {
            true
        } else {
            !blockChannel.contains(channelId) && !blockChannel.contains(channelName)
        }

        val valid = validAllowUser && validAllowChannel && validBlockUser && validBlockChannel

        if (!valid) onAccessError(userId, channelId)

        return valid
    }

    private fun onAccessError(userId: String, channelId: String) {

        val post = Post(
                channelId = channelId,
                message = "Access denied! :rage: \n"
        )

        val ephemeralPost = EphemeralPost(userId, post)
        mattermostService.ephemeralPost(ephemeralPost)
    }

}