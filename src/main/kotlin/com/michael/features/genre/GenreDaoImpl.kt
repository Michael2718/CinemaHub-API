package com.michael.features.genre

import com.michael.features.movie.Movie
import com.michael.features.movie.MovieTable
import com.michael.features.movie.toMovie
import com.michael.features.movieGenre.MovieGenreTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class GenreDaoImpl : GenreDao {
    override suspend fun getAll(): List<Genre> = dbQuery {
        GenreTable.selectAll().map { it.toGenre() }
    }

    override suspend fun getMoviesByGenreId(genreId: Int): List<Movie> = dbQuery {
        MovieGenreTable
            .join(
                MovieTable,
                JoinType.INNER,
                onColumn = MovieGenreTable.movieId,
                otherColumn = MovieTable.movieId
            )
            .selectAll()
            .where { MovieGenreTable.genreId eq genreId }
            .map { it.toMovie() }
    }

    override suspend fun addGenre(genre: Genre): Genre? = dbQuery {
        TODO("Not yet implemented")
    }
}

fun ResultRow.toGenre(): Genre = Genre(
    this[GenreTable.genreId],
    this[GenreTable.name]
)