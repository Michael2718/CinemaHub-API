package com.michael.features.movie

import com.michael.features.favorite.FavoritesTable
import com.michael.features.search.boolOr
import com.michael.features.search.getSearchConditions
import com.michael.features.transaction.TransactionTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

class MovieDaoImpl : MovieDao {
    override suspend fun getAll(
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
    ): List<Movie> = dbQuery {
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

        MoviesTable
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
                MoviesTable.primaryImageUrl
            )
            .where {
                conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
            }
            .groupBy(
                MoviesTable.movieId,
            )
            .orderBy(MoviesTable.voteAverage, SortOrder.DESC)
            .map { it.toMovie() }
    }

    override suspend fun get(movieId: String): Movie? = dbQuery {
        val query = MoviesTable.selectAll().where { MoviesTable.movieId eq movieId }

        query.map { it.toMovie() }.singleOrNull()
    }

    override suspend fun getByUserId(movieId: String, userId: Int): MovieDetailsResponse? = dbQuery {
        val isFavoriteAlias = Expression.build {
            case()
                .When(boolOr(FavoritesTable.userId eq userId), booleanLiteral(true))
                .Else(booleanLiteral(false))
        }.alias("is_favorite")
        val isBoughtAlias = Expression.build {
            case()
                .When(boolOr(TransactionTable.movieId eq movieId), booleanLiteral(true))
                .Else(booleanLiteral(false))
        }.alias("is_bought")

        MoviesTable
            .join(
                otherTable = FavoritesTable,
                joinType = JoinType.LEFT,
                onColumn = MoviesTable.movieId,
                otherColumn = FavoritesTable.movieId
            )
            .join(
                otherTable = TransactionTable,
                joinType = JoinType.LEFT,
                onColumn = MoviesTable.movieId,
                otherColumn = TransactionTable.movieId
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
                isFavoriteAlias,
                isBoughtAlias
            )
            .where {
                (MoviesTable.movieId eq movieId)
            }
            .groupBy(
                MoviesTable.movieId,
            )
            .singleOrNull()?.toMovieDetailsResponse(isFavoriteAlias, isBoughtAlias)
    }

    override suspend fun addMovie(request: AddMovieRequest): Movie? = dbQuery {
        val movieInsertStatement = MoviesTable.insert {
            it[movieId] = request.movieId
            it[title] = request.title
            it[releaseDate] = request.releaseDate
            it[duration] = request.duration
            it[plot] = request.plot
            it[isAdult] = request.isAdult
            it[price] = request.price
            it[primaryImageUrl] = request.primaryImageUrl
        }

        movieInsertStatement.resultedValues?.singleOrNull()?.toMovie()
    }

    override suspend fun deleteMovie(movieId: String): Boolean = dbQuery {
        MoviesTable.deleteWhere { MoviesTable.movieId eq movieId } != 0
    }

    override suspend fun updateMovie(movieId: String, updateRequest: UpdateMovieRequest): Movie? = dbQuery {
        val query = MoviesTable.update({ MoviesTable.movieId eq movieId }) {
            it[title] = updateRequest.title
            it[releaseDate] = updateRequest.releaseDate
            it[duration] = updateRequest.duration
            it[plot] = updateRequest.plot
            it[isAdult] = updateRequest.isAdult
            it[price] = updateRequest.price
            it[primaryImageUrl] = updateRequest.primaryImageUrl
        }
        if (query != 1) null
        else {
            MoviesTable
                .selectAll()
                .where { MoviesTable.movieId eq movieId }
                .map { it.toMovie() }
                .singleOrNull()
        }
    }
}

fun ResultRow.toMovie(): Movie = Movie(
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
    this[MoviesTable.primaryImageUrl]
)

fun ResultRow.toMovieDetailsResponse(
    isFavoriteAlias: Expression<Boolean>,
    isBoughtAlias: Expression<Boolean>
) = MovieDetailsResponse(
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
    this[isFavoriteAlias],
    this[isBoughtAlias]
)
