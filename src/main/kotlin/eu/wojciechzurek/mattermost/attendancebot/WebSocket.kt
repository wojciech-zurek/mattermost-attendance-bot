package eu.wojciechzurek.mattermost.attendancebot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.services.DispatcherService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.kotlin.extra.retry.retryRandomBackoff
import java.net.URI
import java.time.Duration

@Component
class WebSocket(
        private val publisher: ApplicationEventPublisher,
        private val dispatcherService: DispatcherService,
        @Value("\${mattermost.ws.endpoint}") private val wsEndpoint: String,
        @Value("\${mattermost.bot.api.key}") private val apiKey: String

) {

    private val mapper = jacksonObjectMapper()
    private val logger = loggerFor(this.javaClass)

    @EventListener
    fun onApplicationEvent(event: WebSocketConnectionTerminatedEvent) {
        logger.info("WS reconnect attempt.")
        connect()
    }

    @EventListener
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        connect()
    }

    private fun connect() {
        val client = ReactorNettyWebSocketClient()
        val headers = HttpHeaders()
        headers.setBearerAuth(apiKey)
        client.execute(
                URI.create(wsEndpoint),
                headers
        ) { session: WebSocketSession ->
            session.receive()
                    .map { it.payloadAsText }
                    .doOnNext { logger.info("WS Event: {}", it) }
                    .doOnError { logger.error("WS Error: {}", it.message) }
                    .doOnTerminate { logger.info("WS session connection terminate.") }
                    .map { mapper.readValue(it, Event::class.java) }
                    .map { dispatcherService.onNext(it) }
                    .then()

        }.doOnTerminate {
            logger.info("WS connection terminate.")
            publisher.publishEvent(WebSocketConnectionTerminatedEvent(this))
        }
                // .retryBackoff(Long.MAX_VALUE, Duration.ofSeconds(3), Duration.ofSeconds(20))
                .subscribe()
    }
}
