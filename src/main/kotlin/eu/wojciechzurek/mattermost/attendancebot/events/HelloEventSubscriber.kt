package eu.wojciechzurek.mattermost.attendancebot.events

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.principal.Bot
import eu.wojciechzurek.mattermost.attendancebot.principal.BotService
import eu.wojciechzurek.mattermost.attendancebot.services.MattermostService
import org.springframework.stereotype.Component

@Component
class HelloEventSubscriber(
        private val botService: BotService,
        private val mattermostService: MattermostService
) : MattermostEventSubscriber() {

    override fun getType(): String = "hello"

    override fun onEvent(event: Event) {
        event.broadcast.userId?.let { getBotInfo(it) }
    }

    private fun getBotInfo(id: String) {
        mattermostService
                .user(id)
                .map { Bot(it.id, it.userName, it.roles, it.roles.contains("system_admin")) }
                .subscribe { botService.set(it) }
    }
}