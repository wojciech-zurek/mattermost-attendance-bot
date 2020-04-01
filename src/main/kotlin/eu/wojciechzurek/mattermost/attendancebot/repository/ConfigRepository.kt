package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.Config
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ConfigRepository : ReactiveCrudRepository<Config, String> {

    @Query("SELECT * FROM configs WHERE key LIKE :key ORDER BY key ASC")
    fun findByPartialKey(key: String): Flux<Config>
}