package eu.wojciechzurek.mattermost.attendancebot

import org.springframework.context.ApplicationEvent

class WebSocketConnectionTerminatedEvent (source: Any) : ApplicationEvent(source)