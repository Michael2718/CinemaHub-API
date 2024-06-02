package com.michael.features.favorite

interface FavoritesDao {
    suspend fun getAll(): List<Favorite>
    suspend fun getByUserId(userId: Int): List<FavoriteResponse>

    suspend fun isFavorite(userId: Int, movieId: String): Boolean

    suspend fun addFavorite(
        favorite: Favorite
    ): Favorite?

    suspend fun deleteFavorite(userId: Int, movieId: String): Boolean
    suspend fun deleteAllFavorites(userId: Int): Boolean
}
