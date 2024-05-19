package com.michael.features.movieGenre

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class MovieGenreDaoImpl : MovieGenreDao {
    override suspend fun getAll(): List<MovieGenre> = dbQuery {
        MovieGenreTable.selectAll().map { it.toMovieGenre() }
    }

    override suspend fun addMovieGenre(genre: MovieGenre): MovieGenre? {
        TODO("Not yet implemented")
    }

    private fun ResultRow.toMovieGenre(): MovieGenre = MovieGenre(
        this[MovieGenreTable.movieId],
        this[MovieGenreTable.genreId]
    )
}