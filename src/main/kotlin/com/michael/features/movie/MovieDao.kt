package com.michael.features.movie

interface MovieDao {
    suspend fun getAll(): List<Movie>

    suspend fun get(movieId: String): Movie?
    suspend fun getByUserId(movieId: String, userId: Int): MovieDetailsResponse?

    suspend fun addMovie(
        movie: Movie
    ): Movie?
}
