package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.Absence
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AbsencesRepository : ReactiveCrudRepository<Absence, Long> {

    @Query("SELECT * FROM absences WHERE mm_user_id = :user_id ORDER BY id DESC LIMIT 1")
    fun findLatestByUserId(userId: String): Mono<Absence>

    @Query("SELECT * FROM absences WHERE attendance_id = :attendance_id ORDER BY id DESC LIMIT 1")
    fun findByAttendanceId(attendanceId: Long): Mono<Absence>

    @Query("SELECT * FROM absences WHERE mm_user_id = :user_id ORDER BY id DESC LIMIT :limit")
    fun findTopByMMUserId(userId: String, limit: Int): Flux<Absence>
}