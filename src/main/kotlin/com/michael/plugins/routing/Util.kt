package com.michael.plugins.routing

import io.ktor.server.application.*
import kotlinx.datetime.LocalDate
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

data class MovieQueryParams(
    val query: String?,
    val minVoteAverage: Double?,
    val maxVoteAverage: Double?,
    val minReleaseDate: LocalDate?,
    val maxReleaseDate: LocalDate?,
    val minDuration: PGInterval?,
    val maxDuration: PGInterval?,
    val minPrice: PGmoney?,
    val maxPrice: PGmoney?,
    val isAdult: Boolean?
)

fun ApplicationCall.getMovieQueryParams(): MovieQueryParams {
    return MovieQueryParams(
        query = this.request.queryParameters["query"],
        minVoteAverage = this.request.queryParameters["minVoteAverage"]?.toDoubleOrNull(),
        maxVoteAverage = this.request.queryParameters["maxVoteAverage"]?.toDoubleOrNull(),
        minReleaseDate = this.request.queryParameters["minReleaseDate"]?.let { LocalDate.parse(it) },
        maxReleaseDate = this.request.queryParameters["maxReleaseDate"]?.let { LocalDate.parse(it) },
        minDuration = this.request.queryParameters["minDuration"]?.let { PGInterval(it) },
        maxDuration = this.request.queryParameters["maxDuration"]?.let { PGInterval(it) },
        minPrice = this.request.queryParameters["minPrice"]?.let { PGmoney(it.toDouble()) },
        maxPrice = this.request.queryParameters["maxPrice"]?.let { PGmoney(it.toDouble()) },
        isAdult = this.request.queryParameters["isAdult"]?.toBooleanStrictOrNull()
    )
}