package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.Attendance
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface AttendanceRepository : ReactiveCrudRepository<Attendance, Long> {

    @Query("SELECT * FROM attendance WHERE mm_user_id = :user_id AND work_date = :work_date")
    fun findByMMUserIdAndWorkDate(userId: String, workDate: LocalDate): Mono<Attendance>

    @Query("SELECT * FROM attendance WHERE mm_user_id = :user_id ORDER BY id DESC LIMIT 1")
    fun findLatestByMMUserId(userId: String): Mono<Attendance>

    @Query("SELECT * FROM attendance WHERE mm_user_id = :user_id ORDER BY id DESC LIMIT :limit")
    fun findTopByMMUserId(userId: String, limit: Int): Flux<Attendance>
}