package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.ConfigReloadedEvent
import eu.wojciechzurek.mattermost.attendancebot.domain.Config
import eu.wojciechzurek.mattermost.attendancebot.repository.ConfigRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ConfigServiceImpl(
        private val configRepository: ConfigRepository,
        private val publisher: ApplicationEventPublisher
) : ConfigService, ApplicationListener<ApplicationReadyEvent> {

    private val configMap = mutableMapOf<String, String>()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        reloadConfig()
    }

    override fun getOrDefault(key: String, defaultValue: String) = configMap.getOrDefault(key, defaultValue)
    override fun get(key: String): String = configMap[key]!!
    override fun findAll(): Flux<Config> = configRepository.findAll()
    override fun findById(key: String): Mono<Config> = configRepository.findById(key)
    override fun findAllById(keys: List<String>): Flux<Config> = configRepository.findAllById(keys)
    override fun findByPartialKey(key: String): Flux<Config> = configRepository.findByPartialKey(key)

    override fun save(config: Config): Mono<Config> = configRepository
            .save(config)
            .map {
                configMap[it.key] = it.value
                it
            }

    private fun reloadConfig() {
        configRepository
                .findAll()
                .doOnComplete { publisher.publishEvent(ConfigReloadedEvent(this)) }
                .subscribe {
                    configMap[it.key] = it.value
                }
    }
}