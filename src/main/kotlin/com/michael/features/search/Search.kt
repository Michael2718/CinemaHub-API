package com.michael.features.search

import com.michael.types.PGIntervalSerializer
import com.michael.types.PGMoneySerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

@Serializable
data class MovieSearchResponse(
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
    val isFavorite: Boolean
)
