package eu.wojciechzurek.mattermost.attendancebot.events.commands

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Post
import eu.wojciechzurek.mattermost.attendancebot.events.PostedEventSubscriber
import eu.wojciechzurek.mattermost.attendancebot.repository.ConfigRepository
import eu.wojciechzurek.mattermost.attendancebot.services.MattermostService
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired

abstract class CommandSubscriber : PostedEventSubscriber(), InitializingBean {

    @Autowired
    protected lateinit var mattermostService: MattermostService

    @Autowired
    private lateinit var configRepository: ConfigRepository

    protected val configMap = mutableMapOf<String, String>()

    abstract fun getPrefix(): String

    abstract fun getHelp(): String

    abstract fun onEvent(event: Event, message: String)

    override fun afterPropertiesSet() {
        botService.setHelp(getHelp())
        reloadConfig()
    }

    override fun filter(event: Event): Boolean {
        return (event.data.post?.message?.trimStart()?.startsWith(getPrefix()) ?: false) && super.filter(event)
    }

    override fun onEvent(event: Event) {
        onEvent(event, event.data.post?.message?.removePrefix(getPrefix())?.trim()!!)
    }

    override fun onError(event: Event, e: Exception) {
        val post = Post(
                //  userId = it.data.post!!.userId,
                channelId = event.data.post?.channelId!!,
                message = "${event.data.senderName}\n" +
                        "command error!\n" +
                        "exception:\n" +
                        "```\n" +
                        "${e.message}!\n" +
                        "```\n" +
                        "usage: ${getHelp()}"
        )

        mattermostService.post(post)
    }

    protected fun reloadConfig(){
        configRepository.findAll().subscribe {
            configMap[it.key] = it.value
        }
    }
}