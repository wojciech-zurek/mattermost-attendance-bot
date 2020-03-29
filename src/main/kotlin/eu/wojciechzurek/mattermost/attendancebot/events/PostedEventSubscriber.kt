package eu.wojciechzurek.mattermost.attendancebot.events

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.principal.BotService
import org.springframework.beans.factory.annotation.Autowired

abstract class PostedEventSubscriber : MattermostEventSubscriber() {

    @Autowired
    protected lateinit var botService: BotService

    override fun getType(): String = "posted"

    override fun filter(event: Event): Boolean {
        return event.data.userId != botService.get().userId
    }
}