package com.michael.features.genre

import com.michael.features.movie.Movie

interface GenreDao {
    suspend fun getAll(): List<Genre>

    suspend fun getMoviesByGenreId(genreId: Int): List<Movie>?
    suspend fun getAllMovies(): Map<String, List<Movie>>?

    suspend fun addGenre(
        genre: Genre
    ): Genre?
}
