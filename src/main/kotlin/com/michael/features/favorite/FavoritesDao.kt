package com.michael.features.favorite

interface FavoritesDao {
    suspend fun getAll(): List<Favorite>
    suspend fun getByUserId(userId: Int): List<FavoriteResponse>

    suspend fun addFavorite(
        favorite: Favorite
    ): Favorite?
}