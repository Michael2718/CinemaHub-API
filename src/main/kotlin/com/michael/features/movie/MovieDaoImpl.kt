package com.michael.features.movie

import com.michael.features.favorite.FavoritesTable
import com.michael.features.search.MovieSearchResponse
import com.michael.features.search.boolOr
import com.michael.features.search.toMovieSearchResponse
import com.michael.features.transaction.TransactionTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*

class MovieDaoImpl : MovieDao {
    override suspend fun getAll(): List<Movie> = dbQuery {
        MovieTable.selectAll().map { it.toMovie() }
    }

    override suspend fun get(movieId: String): Movie? = dbQuery {
        val query = MovieTable.selectAll().where { MovieTable.movieId eq movieId }

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

        MovieTable
            .join(
                otherTable = FavoritesTable,
                joinType = JoinType.LEFT,
                onColumn = MovieTable.movieId,
                otherColumn = FavoritesTable.movieId
            )
            .join(
                otherTable = TransactionTable,
                joinType = JoinType.LEFT,
                onColumn = MovieTable.movieId,
                otherColumn = TransactionTable.movieId
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
                isFavoriteAlias,
                isBoughtAlias
            )
            .where {
                (MovieTable.movieId eq movieId)
            }
            .groupBy(
                MovieTable.movieId,
            )
            .singleOrNull()?.toMovieDetailsResponse(isFavoriteAlias, isBoughtAlias)
    }

    override suspend fun addMovie(movie: Movie): Movie? = dbQuery {
        val movieInsertStatement = MovieTable.insert {
            it[movieId] = movie.movieId
            it[title] = movie.title
            it[releaseDate] = movie.releaseDate
            it[duration] = movie.duration
            it[voteAverage] = movie.voteAverage
            it[voteCount] = movie.voteCount
            it[plot] = movie.plot
            it[isAdult] = movie.isAdult
            it[popularity] = movie.popularity
            it[price] = movie.price
            it[primaryImageUrl] = movie.primaryImageUrl
        }

        movieInsertStatement.resultedValues?.singleOrNull()?.toMovie()
    }
}

fun ResultRow.toMovie(): Movie = Movie(
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
    this[MovieTable.primaryImageUrl]
)

fun ResultRow.toMovieDetailsResponse(
    isFavoriteAlias: Expression<Boolean>,
    isBoughtAlias: Expression<Boolean>
) = MovieDetailsResponse(
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
    this[isFavoriteAlias],
    this[isBoughtAlias]
)
