package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.domain.User
import eu.wojciechzurek.mattermost.attendancebot.domain.UserMMStatus
import eu.wojciechzurek.mattermost.attendancebot.domain.WorkStatus
import eu.wojciechzurek.mattermost.attendancebot.repository.UserRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.OffsetDateTime
import java.util.*

@Service
class UserServiceImpl(
        private val userRepository: UserRepository,
        private val mattermostService: MattermostService
) : UserService, ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {

        mattermostService
                .users()
                .flatMap {
                    userRepository
                            .findById(it.id)
                            .switchIfEmpty {
                                val now = OffsetDateTime.now()
                                val user = User(it.id, UUID.randomUUID(), it.userName, it.email,
                                        null, null, null, UserMMStatus.UNKNOWN,
                                        WorkStatus.UNKNOWN, now, "", now, now)
                                        .setNew()
                                userRepository.save(user)
                            }
                }.subscribe()
    }
}