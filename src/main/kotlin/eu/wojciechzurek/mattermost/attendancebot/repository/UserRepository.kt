package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveCrudRepository<User, String> {

//    @Query("SELECT * FROM mm_users WHERE mm_user_id = :user_id")
//    fun findByMMUserId(userId: String): Mono<User>

//    @Query("DELETE FROM alerts WHERE public_id = :publicId AND user_id = :userId")
//    fun deleteByPublicIdAndUserId(publicId: UUID, userId: String): Mono<Void>
//
//    @Query("DELETE FROM alerts WHERE public_id = :publicId")
//    fun deleteByPublicId(publicId: UUID): Mono<Void>
}