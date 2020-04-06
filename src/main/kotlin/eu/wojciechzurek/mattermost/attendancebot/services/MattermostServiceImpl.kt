package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import eu.wojciechzurek.mattermost.attendancebot.domain.UserMMStatus
import eu.wojciechzurek.mattermost.attendancebot.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MattermostServiceImpl(
        @Value("\${mattermost.bot.api.key}") private val apiKey: String,
        @Value("\${mattermost.api.endpoint}") private val apiEndpoint: String
) : MattermostService {

    private val logger = loggerFor(this.javaClass)

    private val webClient = WebClient.builder()
            .baseUrl(apiEndpoint)
            .defaultHeader("Authorization", "Bearer $apiKey")
            .build()

    override fun getUserImageEndpoint(userId: String): String = apiEndpoint.plus("/users/$userId/image")

    override fun getFileEndpoint(fileId: String): String = apiEndpoint.plus("/files/$fileId")

    override fun getFilePreviewEndpoint(fileId: String): String = apiEndpoint.plus("/files/$fileId/preview")

    override fun post(body: Any) {
        webClient
                .post()
                .uri("/posts")
                .bodyValue(body)
                .retrieve().onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(Post::class.java)
                .subscribe { logger.info("Post: {}", it) }
    }

    override fun ephemeralPost(body: Any) {
        webClient
                .post()
                .uri("/posts/ephemeral")
                .bodyValue(body)
                .retrieve().onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(Post::class.java)
                .subscribe { logger.info("Post: {}", it) }
    }

    override fun user(id: String): Mono<User> {
        return webClient
                .get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(User::class.java)
                .doOnNext { logger.info("User: {}", it) }
    }

    override fun userName(userName: String): Mono<User> {
        return webClient
                .get()
                .uri("/users/username/{userName}", userName)
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(User::class.java)
                .doOnNext { logger.info("User: {}", it) }
    }

    override fun userSessions(id: String): Flux<UserSession> {
        return webClient
                .get()
                .uri("/users/{id}/sessions", id)
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToFlux(UserSession::class.java)
                .doOnNext { logger.info("User session: {}", it) }
    }

    override fun file(content: ByteArray, filename: String, channelId: String): Mono<FileInfo> =
            file(ByteArrayResource(content), filename, channelId)

    override fun file(resource: ByteArrayResource, filename: String, channelId: String): Mono<FileInfo> {
        val builder = MultipartBodyBuilder()
        builder.part("files", resource).filename(filename)
        builder.part("channel_id", channelId)

        return webClient
                .post()
                .uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }.bodyToMono(FileInfo::class.java)
    }

    override fun publicFileLink(fileId: String): Mono<Link> {
        return webClient
                .get()
                .uri("/files/{fileId}/link", fileId)
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(Link::class.java)
                .doOnNext { logger.info("Link: {}", it) }
    }

    override fun channelMembers(channelId: String): Flux<ChannelMember> {
        return webClient
                .get()
                .uri("/channels/{channelId}/members?per_page=200", channelId)
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToFlux(ChannelMember::class.java)
                .doOnNext { logger.info("User: {}", it) }
    }

    override fun users(): Flux<User> {
        return webClient
                .get()
                .uri("/users?per_page=200")
                .retrieve()
                .onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToFlux(User::class.java)
                .doOnNext { logger.info("User: {}", it) }
    }

    override fun directMessageChannel(users: List<String>): Mono<ChannelInfo> {
        return webClient
                .post()
                .uri("/channels/direct")
                .bodyValue(users)
                .retrieve().onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(ChannelInfo::class.java)
                .doOnNext { logger.info("Direct channel: {}", it) }
    }

    override fun userStatus(id: String): Mono<UserStatus> {
        return webClient
                .get()
                .uri("/users/{id}/status", id)
                .retrieve().onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(UserStatus::class.java)
                .doOnNext { logger.info("User status: {}", it) }
    }

    override fun userStatus(userStatus: UserStatus): Mono<UserStatus> {
        return webClient
                .put()
                .uri("/users/{id}/status", userStatus.userId)
                .bodyValue(userStatus)
                .retrieve().onStatus(HttpStatus::isError) {
                    it.bodyToMono<String>().subscribe { body -> logger.error(body) }
                    Mono.error(MattermostException(it.statusCode(), "Mattermost Endpoint exception"))
                }
                .bodyToMono(UserStatus::class.java)
                .doOnNext { logger.info("User status: {}", it) }
    }
}