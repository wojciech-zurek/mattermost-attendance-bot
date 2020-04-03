package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.*
import org.springframework.core.io.ByteArrayResource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MattermostService {
    fun post(body: Any)
    fun user(id: String): Mono<User>
    fun userName(userName: String): Mono<User>
    fun file(content: ByteArray, filename: String, channelId: String): Mono<FileInfo>
    fun file(resource: ByteArrayResource, filename: String, channelId: String): Mono<FileInfo>
    fun publicFileLink(fileId: String): Mono<Link>
    fun channelMembers(channelId: String): Flux<ChannelMember>
    fun getUserImageEndpoint(userId: String): String
    fun getFileEndpoint(fileId: String): String
    fun getFilePreviewEndpoint(fileId: String): String
    fun ephemeralPost(body: Any)
    fun users(): Flux<User>
    fun directMessageChannel(users: List<String>): Mono<ChannelInfo>
}