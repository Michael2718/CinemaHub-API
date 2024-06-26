package com.michael.features.statistics

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

@Serializable
data class GenresStats(
    val genreName: String,
    val movieCount: Int,
    val totalWatchedTimes: Int,
    val averageVote: Double
)

@Serializable
data class UsersStats(
    val username: String,
    val birthDate: LocalDate,
    val watchedHours: Double,
    val watchedMovieCount: Int,
    val favoriteGenre: String
)


object UsersStatsView : Table("users_stats") {
    val username = varchar("username", 50)
    val birthDate = date("birth_date")
    val watchedHours = double("watched_hours")
    val watchedMovieCount = integer("watched_movie_count")
    val favoriteGenre = varchar("favorite_genre", 50)
}
