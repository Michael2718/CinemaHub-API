package com.michael.features.movie

import com.michael.features.search.MovieSearchResponse

interface MovieDao {
    suspend fun getAll(): List<Movie>

    suspend fun get(movieId: String): Movie?
    suspend fun getByUserId(movieId: String, userId: Int): MovieSearchResponse?

    suspend fun addMovie(
        movie: Movie
    ): Movie?
}
