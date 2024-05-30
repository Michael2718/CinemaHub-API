package com.michael.features.movie

interface MovieDao {
    suspend fun getAll(): List<Movie>

    suspend fun getByMovieId(movieId: String): Movie?

    suspend fun addMovie(
        movie: Movie
    ): Movie?
}
