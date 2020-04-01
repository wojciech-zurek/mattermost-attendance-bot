//package eu.wojciechzurek.mattermost.attendancebot.events.commands
//
//import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
//
//abstract class AccessCommandSubscriber : CommandSubscriber() {
//
//    private val allowedChannel = listOf<String>()
//    private val allowedUserName = listOf<String>()
//
//    override fun filter(event: Event): Boolean {
//
//        event.data.userId
//        event.data.senderName
//
//        event.data.post?.channelId
//        event.data.channelName
//
//
//        return (isAllowedChannel(event.data.channelName!!) || isAllowedUserName(event.data.senderName!!))
//                && super.filter(event)
//    }
//
//    private fun isAllowedChannel(id: String, name: String): Boolean = allowedChannel.contains(id) || allowedChannel.contains(name)
//
//    private fun isAllowedUserName(name: String): Boolean = allowedUserName.contains(name)
//
//    private fun mm() = mattermostService.user().
//}