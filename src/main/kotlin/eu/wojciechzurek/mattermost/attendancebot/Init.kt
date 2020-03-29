package eu.wojciechzurek.mattermost.attendancebot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event
import eu.wojciechzurek.mattermost.attendancebot.services.DispatcherService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.net.URI

@Component
class Init(
        private val dispatcherService: DispatcherService,
        @Value("\${mattermost.ws.endpoint}") private val wsEndpoint: String,
        @Value("\${mattermost.bot.api.key}") private val apiKey: String
) : ApplicationListener<ApplicationReadyEvent> {

    private val mapper = jacksonObjectMapper()
    private val logger = loggerFor(this.javaClass)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
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
                    .map { mapper.readValue(it, Event::class.java) }
//                    .doOnNext { println(it) }
                    .map { dispatcherService.onNext(it) }
                    .then()
        }.subscribe()
    }
}
