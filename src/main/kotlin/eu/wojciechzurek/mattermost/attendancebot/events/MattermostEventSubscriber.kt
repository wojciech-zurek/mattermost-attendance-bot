package eu.wojciechzurek.mattermost.attendancebot.events

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.reactivestreams.Subscription
import reactor.core.publisher.BaseSubscriber

abstract class MattermostEventSubscriber : BaseSubscriber<Event>() {

    private val logger = loggerFor(this.javaClass)

    abstract fun getType(): String
    abstract fun onEvent(event: Event)

    final override fun hookOnNext(event: Event) {
        try {
            if (event.event == getType() && filter(event)) onEvent(event)
        } catch (e: Exception) {
            logger.error(e.message, e)
            onError(event, e)
        }
    }

    open fun onError(event: Event, e: Exception) = Unit

    open fun filter(event: Event): Boolean = true

    override fun hookOnSubscribe(subscription: Subscription) {
        super.hookOnSubscribe(subscription)
        logger.info("Subscription started: {}", this.javaClass.simpleName)
    }
}