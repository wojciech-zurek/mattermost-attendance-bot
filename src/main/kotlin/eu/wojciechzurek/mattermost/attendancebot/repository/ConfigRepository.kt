package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.Config
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ConfigRepository: ReactiveCrudRepository<Config, String> {
}