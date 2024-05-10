package com.michael.dao

import com.michael.models.Movie
import com.michael.models.MovieTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class MovieDaoImpl : MovieDao {
    override suspend fun getAll(): List<Movie> = dbQuery {
        MovieTable.selectAll().map { it.toMovie() }
    }

    override suspend fun addMovie(movie: Movie): Movie? = dbQuery {
        val movieInsertStatement = MovieTable.insert {
            it[movieId] = movie.movieId
            it[adult] = movie.adult
            it[overview] = movie.overview
            it[popularity] = movie.popularity
            it[releaseDate] = movie.releaseDate
            it[title] = movie.title
            it[voteAverage] = movie.voteAverage
            it[voteCount] = movie.voteCount
            it[duration] = movie.duration
            it[price] = movie.price
            it[genreId] = movie.genreId
        }

        movieInsertStatement.resultedValues?.singleOrNull()?.toMovie()
    }

    private fun ResultRow.toMovie(): Movie {
        return Movie(
            this[MovieTable.movieId],
            this[MovieTable.adult],
            this[MovieTable.overview],
            this[MovieTable.popularity],
            this[MovieTable.releaseDate],
            this[MovieTable.title],
            this[MovieTable.voteAverage],
            this[MovieTable.voteCount],
            this[MovieTable.duration],
            this[MovieTable.price],
            this[MovieTable.genreId]
        )
    }
}