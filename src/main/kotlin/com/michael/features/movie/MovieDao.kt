package com.michael.features.movie

interface MovieDao {
    suspend fun getAll(): List<Movie>

    suspend fun get(movieId: String): Movie?
    suspend fun getByUserId(movieId: String, userId: Int): MovieDetailsResponse?

    suspend fun addMovie(
        request: AddMovieRequest
    ): Movie?

    suspend fun deleteMovie(movieId: String): Boolean

    suspend fun updateMovie(movieId: String, updateRequest: UpdateMovieRequest): Movie?
}
