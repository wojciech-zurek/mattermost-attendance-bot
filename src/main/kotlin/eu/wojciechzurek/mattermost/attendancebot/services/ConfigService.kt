package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.domain.Config
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConfigService {
    fun getOrDefault(key: String, defaultValue: String): String
    fun findAll(): Flux<Config>
    fun findById(key: String): Mono<Config>
    fun save(config: Config): Mono<Config>
    fun findAllById(keys: List<String>): Flux<Config>
}