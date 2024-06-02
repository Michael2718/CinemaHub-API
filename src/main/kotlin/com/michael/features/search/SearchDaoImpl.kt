package com.michael.features.search

import com.michael.features.favorite.FavoritesTable
import com.michael.features.favorite.FavoritesTable.bool
import com.michael.features.movie.MovieTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import com.michael.types.PGIntervalGreaterEqOp
import com.michael.types.PGIntervalLessEqOp
import com.michael.types.PGMoneyGreaterEqOp
import com.michael.types.PGMoneyLessEqOp
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.asLiteral
import org.jetbrains.exposed.sql.SqlExpressionBuilder.case
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

class SearchDaoImpl : SearchDao {
//    override suspend fun searchMovies(
//        query: String?,
//        minVoteAverage: Double?,
//        maxVoteAverage: Double?,
//        minReleaseDate: LocalDate?,
//        maxReleaseDate: LocalDate?,
//        minDuration: PGInterval?,
//        maxDuration: PGInterval?,
//        minPrice: PGmoney?,
//        maxPrice: PGmoney?,
//        isAdult: Boolean?
//    ): List<Movie> = dbQuery {
//        val conditions = getSearchConditions(
//            query,
//            minVoteAverage,
//            maxVoteAverage,
//            minReleaseDate,
//            maxReleaseDate,
//            minDuration,
//            maxDuration,
//            minPrice,
//            maxPrice,
//            isAdult,
//        )
//
//        MovieTable
//            .selectAll()
//            .where {
//                conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
//            }
//            .mapNotNull { it.toMovie() }
//    }

    override suspend fun searchMovies(
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
        userId: Int
    ): List<MovieSearchResponse> = dbQuery {
        val table = MovieTable
        val conditions = getSearchConditions(
            query,
            minVoteAverage,
            maxVoteAverage,
            minReleaseDate,
            maxReleaseDate,
            minDuration,
            maxDuration,
            minPrice,
            maxPrice,
            isAdult,
        )

        val isFavoriteAlias = Expression.build {
            case()
                .When(FavoritesTable.userId eq userId, booleanLiteral(true))
                .Else(booleanLiteral(false))
        }.alias("is_favorite")

        val result = table
            .join(
                otherTable = FavoritesTable,
                joinType = JoinType.LEFT,
                onColumn = MovieTable.movieId,
                otherColumn = FavoritesTable.movieId
            )
            .select(
                MovieTable.movieId,
                MovieTable.title,
                MovieTable.releaseDate,
                MovieTable.duration,
                MovieTable.voteAverage,
                MovieTable.voteCount,
                MovieTable.plot,
                MovieTable.isAdult,
                MovieTable.popularity,
                MovieTable.price,
                MovieTable.primaryImageUrl,
                isFavoriteAlias
            )
            .groupBy(
                MovieTable.movieId,
                MovieTable.title,
                MovieTable.releaseDate,
                MovieTable.duration,
                MovieTable.voteAverage,
                MovieTable.voteCount,
                MovieTable.plot,
                MovieTable.isAdult,
                MovieTable.popularity,
                MovieTable.price,
                MovieTable.primaryImageUrl,
                isFavoriteAlias
            )
//            .selectAll()
            .where {
                conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
            }
        result.mapNotNull { it.toMovieSearchResponse(isFavoriteAlias) }
    }

    private fun getSearchConditions(
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
    ): MutableList<Op<Boolean>> {
        val conditions = mutableListOf<Op<Boolean>>()

        query?.let {
            conditions.add(
                (MovieTable.title.lowerCase() like "%${it.lowercase()}%") or
                        (MovieTable.plot.lowerCase() like "%${it.lowercase()}%")
            )
        }

        minVoteAverage?.let { conditions.add(MovieTable.voteAverage greaterEq it) }
        maxVoteAverage?.let { conditions.add(MovieTable.voteAverage lessEq it) }

        minReleaseDate?.let { conditions.add(MovieTable.releaseDate greaterEq it) }
        maxReleaseDate?.let { conditions.add(MovieTable.releaseDate lessEq it) }

        minDuration?.let { conditions.add(PGIntervalGreaterEqOp(MovieTable.duration, it)) }
        maxDuration?.let { conditions.add(PGIntervalLessEqOp(MovieTable.duration, it)) }

        minPrice?.let { conditions.add(PGMoneyGreaterEqOp(MovieTable.price, it)) }
        maxPrice?.let { conditions.add(PGMoneyLessEqOp(MovieTable.price, it)) }

        isAdult?.let { conditions.add(MovieTable.isAdult eq it) }

        return conditions
    }
}

fun ResultRow.toMovieSearchResponse(isFavoriteAlias: Expression<Boolean>) = MovieSearchResponse(
    this[MovieTable.movieId],
    this[MovieTable.title],
    this[MovieTable.releaseDate],
    this[MovieTable.duration],
    this[MovieTable.voteAverage],
    this[MovieTable.voteCount],
    this[MovieTable.plot],
    this[MovieTable.isAdult],
    this[MovieTable.popularity],
    this[MovieTable.price],
    this[MovieTable.primaryImageUrl],
    this[isFavoriteAlias]
)
