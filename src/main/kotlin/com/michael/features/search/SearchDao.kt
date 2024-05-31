package com.michael.features.search

import com.michael.features.movie.Movie
import kotlinx.datetime.LocalDate
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

interface SearchDao {
    suspend fun searchMovies(
        query: String?,
        minVoteAverage: Double?,
        maxVoteAverage: Double?,
        minReleaseDate: LocalDate?,
        maxReleaseDate: LocalDate?,
        minDuration: PGInterval?,
        maxDuration: PGInterval?,
        minPrice: PGmoney?,
        maxPrice: PGmoney?,
        isAdult: Boolean?
    ): List<Movie>
}