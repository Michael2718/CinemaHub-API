package com.michael.models

import com.michael.types.interval
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.duration
import kotlin.time.Duration

@Serializable
data class Movie(
    val movieId: Int,
    val adult: Boolean,
    val overview: String,
    val popularity: Int,
    val releaseDate: LocalDate,
    val title: String,
    val voteAverage: Double,
    val voteCount: Int,
    val duration: Duration,
    val price: Double,
    val genreId: Int
)

object MovieTable : Table("movie") {
    val movieId = integer("movie_id")
    val adult = bool("adult")
    val overview = varchar("overview", 256)
    val popularity = integer("popularity")
    val releaseDate = date("release_date")
    val title = varchar("title", 256)
    val voteAverage = double("vote_average")
    val voteCount = integer("vote_count")
    val duration = interval("duration")
    val price = double("price")
    val genreId = integer("genre_id") // TODO: references GenreTable.id

    override val primaryKey = PrimaryKey(movieId)
}