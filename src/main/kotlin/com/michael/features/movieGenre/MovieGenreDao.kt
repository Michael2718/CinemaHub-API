package com.michael.features.movieGenre

interface MovieGenreDao {
    suspend fun getAll(): List<MovieGenre>

    suspend fun addMovieGenre(
        genre: MovieGenre
    ): MovieGenre?
}