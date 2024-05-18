package com.michael.features.genre

interface GenreDao {
    suspend fun getAll(): List<Genre>

    suspend fun addGenre(
        genre: Genre
    ): Genre?
}
