package eu.wojciechzurek.mattermost.attendancebot.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.wojciechzurek.mattermost.attendancebot.domain.ReportByDate
import eu.wojciechzurek.mattermost.attendancebot.domain.ReportByUserName
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDate

@Repository
class ReportRepositoryImpl(private val databaseClient: DatabaseClient) : ReportRepository {

    private val objectMapper = jacksonObjectMapper()

    override fun users(month: Int, year: Int): Flux<String> {
        return databaseClient
                .execute("SELECT mm_user_name FROM attendance LEFT JOIN mm_users ON attendance.mm_user_id = mm_users.mm_user_id " +
                        "WHERE EXTRACT(month FROM work_date) = :month AND EXTRACT(year FROM work_date) = :year GROUP BY mm_user_name ORDER BY mm_user_name")
                .bind("month", month)
                .bind("year", year)
                .map { row, _ ->
                    row.get("mm_user_name", String::class.java)!!
                }
                .all()
    }


    override fun reportByUserName(month: Int, year: Int): Flux<ReportByUserName> {
        return databaseClient
                .execute("SELECT mm_user_name, array_to_json(array_agg(work_date::TEXT)) AS work_date FROM attendance LEFT JOIN mm_users ON attendance.mm_user_id = mm_users.mm_user_id " +
                        "WHERE EXTRACT(month FROM work_date) = :month AND EXTRACT(year FROM work_date) = :year GROUP BY mm_user_name ORDER BY mm_user_name")
                .bind("month", month)
                .bind("year", year)
                .map { row, _ ->
                    ReportByUserName(
                            row.get("mm_user_name", String::class.java)!!,
                            objectMapper.readValue(row.get("work_date", String::class.java)!!, classOfList(mutableListOf<String>()))
                    )
                }
                .all()
    }

    override fun reportByDate(start: LocalDate, stop: LocalDate): Flux<ReportByDate> {
        return databaseClient
                .execute("SELECT d.date AS w_date, array_to_json(array_agg(mm_user_name::TEXT)) AS mm_user_name FROM generate_series(:start, :stop, '1 day'::interval) d LEFT JOIN attendance ON attendance.work_date = d.date LEFT JOIN mm_users ON attendance.mm_user_id = mm_users.mm_user_id GROUP BY w_date ORDER BY w_date")
                .bind("start", start)
                .bind("stop", stop)
                .map { row, _ ->
                    val x = row.get("mm_user_name", String::class.java)!!
                    ReportByDate(
                            row.get("w_date", LocalDate::class.java)!!,
                            objectMapper.readValue(x, classOfList(mutableListOf<String>()))
                    )
                }
                .all()
    }
}

inline fun <reified T : Any> classOfList(list: List<T>) = list::class.java