package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.events.MattermostEventSubscriber


interface DispatcherService {

    fun subscribe(s: MattermostEventSubscriber)
    fun onNext(event: Event)
}