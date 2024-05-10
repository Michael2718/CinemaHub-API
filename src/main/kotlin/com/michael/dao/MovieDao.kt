package com.michael.dao

import com.michael.models.Movie
import kotlinx.datetime.LocalDate
import org.postgresql.util.PGInterval

interface MovieDao {
    suspend fun getAll(): List<Movie>
    // Add other methods for searching, filtering etc.
    //    suspend fun getMovieById(movieId: Int): Movie?
    //    suspend fun getMoviesByGenre(genreId: Int): List<Movie>

    suspend fun addMovie(
        movie: Movie
    ): Movie?

//    suspend fun addMovie(
//        adult: Boolean,
//        overview: String,
//        popularity: Int,
//        releaseDate: LocalDate,
//        title: String,
//        voteAverage: Double,
//        voteCount: Int,
//        duration: PGInterval,
//        price: Double,
//        genreId: Int
//    ): Movie?
}
