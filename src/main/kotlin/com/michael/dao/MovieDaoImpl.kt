package com.michael.dao

import com.michael.models.Movie
import com.michael.models.MovieTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class MovieDaoImpl : MovieDao {
    override suspend fun getAll(): List<Movie> = dbQuery {
        MovieTable.selectAll().map { it.toMovie() }
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