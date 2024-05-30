package com.michael.features.movie

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class MovieDaoImpl : MovieDao {
    override suspend fun getAll(): List<Movie> = dbQuery {
        MovieTable.selectAll().map { it.toMovie() }
    }

    override suspend fun getByMovieId(movieId: String): Movie? = dbQuery {
        val query = MovieTable.selectAll().where { MovieTable.movieId eq movieId }

        query.map { it.toMovie() }.singleOrNull()
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

    private fun ResultRow.toMovie(): Movie = Movie(
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
}