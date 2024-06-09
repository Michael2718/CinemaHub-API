package com.michael.features.movie

import com.michael.types.PGIntervalSerializer
import com.michael.types.PGMoneySerializer
import com.michael.types.interval
import com.michael.types.money
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

@Serializable
data class Movie(
    val movieId: String,
    val title: String,
    val releaseDate: LocalDate,
    @Serializable(with = PGIntervalSerializer::class)
    val duration: PGInterval,
    val voteAverage: Double,
    val voteCount: Int,
    val plot: String,
    val isAdult: Boolean,
    val popularity: Int,
    @Serializable(with = PGMoneySerializer::class)
    val price: PGmoney,
    val primaryImageUrl: String
)

object MoviesTable : Table("movie") {
    val movieId = varchar("movie_id", 10)
    val title = varchar("title", 256)
    val releaseDate = date("release_date")
    val duration = interval("duration")
    val voteAverage = double("vote_average")
    val voteCount = integer("vote_count")
    val plot = varchar("plot", 512)
    val isAdult = bool("is_adult")
    val popularity = integer("popularity")
    val price = money("price")
    val primaryImageUrl = varchar("primary_image_url", 512)

    override val primaryKey = PrimaryKey(movieId)
}

@Serializable
data class MovieDetailsResponse(
    val movieId: String,
    val title: String,
    val releaseDate: LocalDate,
    @Serializable(with = PGIntervalSerializer::class)
    val duration: PGInterval,
    val voteAverage: Double,
    val voteCount: Int,
    val plot: String,
    val isAdult: Boolean,
    val popularity: Int,
    @Serializable(with = PGMoneySerializer::class)
    val price: PGmoney,
    val primaryImageUrl: String,
    val isFavorite: Boolean,
    val isBought: Boolean
)

@Serializable
data class AddMovieRequest(
    val movieId: String,
    val title: String,
    val releaseDate: LocalDate,
    @Serializable(with = PGIntervalSerializer::class)
    val duration: PGInterval,
    val plot: String,
    val isAdult: Boolean,
    @Serializable(with = PGMoneySerializer::class)
    val price: PGmoney,
    val primaryImageUrl: String
)

@Serializable
data class UpdateMovieRequest(
    val title: String,
    val releaseDate: LocalDate,
    @Serializable(with = PGIntervalSerializer::class)
    val duration: PGInterval,
    val plot: String,
    val isAdult: Boolean,
    @Serializable(with = PGMoneySerializer::class)
    val price: PGmoney,
    val primaryImageUrl: String
)
