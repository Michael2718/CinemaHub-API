package com.michael.features.movie

import kotlinx.datetime.LocalDate
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

interface MovieDao {
    suspend fun getAll(
        query: String?,
        minVoteAverage: Double?,
        maxVoteAverage: Double?,
        minReleaseDate: LocalDate?,
        maxReleaseDate: LocalDate?,
        minDuration: PGInterval?,
        maxDuration: PGInterval?,
        minPrice: PGmoney?,
        maxPrice: PGmoney?,
        isAdult: Boolean?,
    ): List<Movie>

    suspend fun get(movieId: String): Movie?
    suspend fun getByUserId(movieId: String, userId: Int): MovieDetailsResponse?

    suspend fun addMovie(
        request: AddMovieRequest
    ): Movie?

    suspend fun deleteMovie(movieId: String): Boolean

    suspend fun updateMovie(movieId: String, updateRequest: UpdateMovieRequest): Movie?
}
