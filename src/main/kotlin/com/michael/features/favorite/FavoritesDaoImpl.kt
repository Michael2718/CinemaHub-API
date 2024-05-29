package com.michael.features.favorite

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class FavoritesDaoImpl : FavoritesDao {
    override suspend fun getAll(): List<Favorite> = dbQuery {
        FavoritesTable.selectAll().map { it.toFavorite() }
    }

    override suspend fun getByUserId(userId: Int): List<Favorite> = dbQuery {
        FavoritesTable
            .selectAll()
            .where { FavoritesTable.userId eq userId }
            .map { it.toFavorite() }
    }

    override suspend fun addFavorite(favorite: Favorite): Favorite? = dbQuery {
        val favoriteInsertStatement = FavoritesTable.insert {
            it[userId] = favorite.userId
            it[movieId] = favorite.movieId
            it[addedDate] = favorite.addedDate
        }

        favoriteInsertStatement.resultedValues?.singleOrNull()?.toFavorite()
    }

    private fun ResultRow.toFavorite(): Favorite = Favorite(
        this[FavoritesTable.userId],
        this[FavoritesTable.movieId],
        this[FavoritesTable.addedDate]
    )
}