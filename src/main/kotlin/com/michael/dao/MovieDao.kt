package com.michael.dao

import com.michael.models.Movie

interface MovieDao {
    suspend fun getAll(): List<Movie>
    // Add other methods for searching, filtering etc.
    //    suspend fun getMovieById(movieId: Int): Movie?
    //    suspend fun getMoviesByGenre(genreId: Int): List<Movie>
}
