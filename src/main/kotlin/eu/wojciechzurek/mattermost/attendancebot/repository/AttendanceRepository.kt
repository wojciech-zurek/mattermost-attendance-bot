package eu.wojciechzurek.mattermost.attendancebot.repository

import eu.wojciechzurek.mattermost.attendancebot.domain.Attendance
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDate

interface AttendanceRepository : ReactiveCrudRepository<Attendance, Long> {

    @Query("SELECT * FROM attendance WHERE mm_user_id = :user_id AND work_date = :work_date")
    fun findByMMUserIdAndWorkDate(userId: String, workDate: LocalDate): Mono<Attendance>

//    @Query("DELETE FROM alerts WHERE public_id = :publicId AND user_id = :userId")
//    fun deleteByPublicIdAndUserId(publicId: UUID, userId: String): Mono<Void>
//
//    @Query("DELETE FROM alerts WHERE public_id = :publicId")
//    fun deleteByPublicId(publicId: UUID): Mono<Void>
}