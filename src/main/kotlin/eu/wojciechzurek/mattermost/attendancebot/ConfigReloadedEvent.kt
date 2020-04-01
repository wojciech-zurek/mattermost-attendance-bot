package eu.wojciechzurek.mattermost.attendancebot

import org.springframework.context.ApplicationEvent

class ConfigReloadedEvent(source: Any) : ApplicationEvent(source)