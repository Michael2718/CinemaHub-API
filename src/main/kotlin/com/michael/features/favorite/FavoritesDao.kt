package com.michael.features.favorite

import com.michael.features.movie.Movie

interface FavoritesDao {
    suspend fun getAll(): List<Favorite>
    suspend fun getByUserId(userId: Int): List<Favorite>

    suspend fun addFavorite(
        favorite: Favorite
    ): Favorite?
}