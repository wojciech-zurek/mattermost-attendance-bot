package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.events.MattermostEventSubscriber
import org.springframework.stereotype.Service
import reactor.core.publisher.EmitterProcessor

@Service
class DispatcherServiceImpl(list: List<MattermostEventSubscriber>) : DispatcherService {

    private val emitter = EmitterProcessor.create<Event>().also { ep ->
        list.forEach { ep.subscribe(it) }
    }

    override fun subscribe(s: MattermostEventSubscriber) = emitter.subscribe(s)

    override fun onNext(event: Event) {
        emitter.onNext(event)
    }
}