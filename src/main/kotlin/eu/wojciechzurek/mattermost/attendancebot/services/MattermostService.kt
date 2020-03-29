package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.ChannelMember
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.FileInfo
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Link
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.User
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
}