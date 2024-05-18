package com.michael.features.favorite

interface FavoriteDao {
    suspend fun getAll(): List<Favorite>

    suspend fun addFavorite(
        favorite: Favorite
    ): Favorite?
}