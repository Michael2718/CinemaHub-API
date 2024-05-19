package com.michael.features.history

import com.michael.types.PGIntervalSerializer
import com.michael.types.interval
import com.michael.utils.todayDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.postgresql.util.PGInterval

@Serializable
data class History(
    val userId: Int,
    val movieId: String,
    val watchedDate: LocalDateTime = todayDateTime(),
    @Serializable(with = PGIntervalSerializer::class)
    val watchedDuration: PGInterval
)

object HistoryTable : Table("history") {
    val userId = integer("user_id")
    val movieId = varchar("movie_id", 10)
    val watchedDate = datetime("watched_date")
    val watchedDuration = interval("watched_duration")

    override val primaryKey = PrimaryKey(userId, movieId, watchedDate)
}
