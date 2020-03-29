package eu.wojciechzurek.mattermost.attendancebot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("mm_users")
data class User(

        @Id
        @Column("mm_user_id")
        val userId: String,

        val publicId: UUID,

        @Column("mm_user_name")
        val userName: String,

        @Column("mm_user_email")
        val userEmail: String?,

        @Column("mm_channel_id")
        val channelId: String,

        @Column("mm_channel_name")
        val channelName: String,

        @Column("mm_channel_display_name")
        val channelDisplayName: String,

        @Column("mm_status")
        val userStatus: UserStatus,

        val createDate: LocalDateTime

) : Persistable<String> {

    @Transient
    private var new = false

    override fun isNew(): Boolean = new

    fun setNew(new: Boolean): User {
        this.new = new
        return this
    }

    override fun getId(): String = userId
}