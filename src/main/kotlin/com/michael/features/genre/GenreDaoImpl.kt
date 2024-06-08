package com.michael.features.genre

import com.michael.features.movie.Movie
import com.michael.features.movie.MoviesTable
import com.michael.features.movie.toMovie
import com.michael.features.movieGenre.MovieGenreTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class GenreDaoImpl : GenreDao {
    override suspend fun getAll(): List<Genre> = dbQuery {
        GenreTable.selectAll().map { it.toGenre() }
    }

    override suspend fun getMoviesByGenreId(genreId: Int): List<Movie> = dbQuery {
        MovieGenreTable
            .join(
                MoviesTable,
                JoinType.INNER,
                onColumn = MovieGenreTable.movieId,
                otherColumn = MoviesTable.movieId
            )
            .selectAll()
            .where { MovieGenreTable.genreId eq genreId }
            .orderBy(MoviesTable.voteAverage, SortOrder.DESC)
            .map { it.toMovie() }
    }

    override suspend fun getAllMovies(): Map<String, List<Movie>>? = dbQuery {
        val genres = GenreTable.selectAll().map { it.toGenre() }
        val result = genres.associate { genre ->
            genre.name to MovieGenreTable
                .join(
                    MoviesTable,
                    JoinType.INNER,
                    onColumn = MovieGenreTable.movieId,
                    otherColumn = MoviesTable.movieId
                )
                .selectAll()
                .where { MovieGenreTable.genreId eq genre.genreId }
                .orderBy(MoviesTable.voteAverage, SortOrder.DESC)
                .map { it.toMovie() }
        }
        result.ifEmpty { null }
    }

    override suspend fun addGenre(genre: Genre): Genre? = dbQuery {
        TODO("Not yet implemented")
    }
}

fun ResultRow.toGenre(): Genre = Genre(
    this[GenreTable.genreId],
    this[GenreTable.name]
)
