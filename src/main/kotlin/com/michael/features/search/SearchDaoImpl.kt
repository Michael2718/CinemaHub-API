package com.michael.features.search

import com.michael.features.favorite.FavoritesTable
import com.michael.features.movie.MoviesTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import com.michael.types.PGIntervalGreaterEqOp
import com.michael.types.PGIntervalLessEqOp
import com.michael.types.PGMoneyGreaterEqOp
import com.michael.types.PGMoneyLessEqOp
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

class SearchDaoImpl : SearchDao {
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
                .When(boolOr(FavoritesTable.userId eq userId), booleanLiteral(true))
                .Else(booleanLiteral(false))
        }.alias("is_favorite")

        MoviesTable
            .join(
                otherTable = FavoritesTable,
                joinType = JoinType.LEFT,
                onColumn = MoviesTable.movieId,
                otherColumn = FavoritesTable.movieId
            )
            .select(
                MoviesTable.movieId,
                MoviesTable.title,
                MoviesTable.releaseDate,
                MoviesTable.duration,
                MoviesTable.voteAverage,
                MoviesTable.voteCount,
                MoviesTable.plot,
                MoviesTable.isAdult,
                MoviesTable.popularity,
                MoviesTable.price,
                MoviesTable.primaryImageUrl,
                isFavoriteAlias
            )
            .where {
                conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
            }
            .groupBy(
                MoviesTable.movieId,
            )
            .orderBy(MoviesTable.voteAverage, SortOrder.DESC)
            .mapNotNull { it.toMovieSearchResponse(isFavoriteAlias) }
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
                (MoviesTable.title.lowerCase() like "%${it.lowercase()}%") or
                        (MoviesTable.plot.lowerCase() like "%${it.lowercase()}%")
            )
        }

        minVoteAverage?.let { conditions.add(MoviesTable.voteAverage greaterEq it) }
        maxVoteAverage?.let { conditions.add(MoviesTable.voteAverage lessEq it) }

        minReleaseDate?.let { conditions.add(MoviesTable.releaseDate greaterEq it) }
        maxReleaseDate?.let { conditions.add(MoviesTable.releaseDate lessEq it) }

        minDuration?.let { conditions.add(PGIntervalGreaterEqOp(MoviesTable.duration, it)) }
        maxDuration?.let { conditions.add(PGIntervalLessEqOp(MoviesTable.duration, it)) }

        minPrice?.let { conditions.add(PGMoneyGreaterEqOp(MoviesTable.price, it)) }
        maxPrice?.let { conditions.add(PGMoneyLessEqOp(MoviesTable.price, it)) }

        isAdult?.let { conditions.add(MoviesTable.isAdult eq it) }

        return conditions
    }
}

fun ResultRow.toMovieSearchResponse(isFavoriteAlias: Expression<Boolean>) = MovieSearchResponse(
    this[MoviesTable.movieId],
    this[MoviesTable.title],
    this[MoviesTable.releaseDate],
    this[MoviesTable.duration],
    this[MoviesTable.voteAverage],
    this[MoviesTable.voteCount],
    this[MoviesTable.plot],
    this[MoviesTable.isAdult],
    this[MoviesTable.popularity],
    this[MoviesTable.price],
    this[MoviesTable.primaryImageUrl],
    this[isFavoriteAlias]
)

class BoolOr(
    private val expression: Expression<Boolean>,
) : Function<Boolean>(BooleanColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("bool_or(", expression, ")")
        }
    }
}

fun boolOr(expr: Expression<Boolean>) = BoolOr(expr)