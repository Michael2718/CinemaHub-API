package com.michael.features.movieGenre

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class MovieGenre(
    val movieId: String,
    val genreId: Int
)

object MovieGenreTable : Table("genre") {
    val movieId = varchar("movie_id", 10)
    val genreId = integer("genre_id")

    override val primaryKey = PrimaryKey(movieId, genreId)
}
