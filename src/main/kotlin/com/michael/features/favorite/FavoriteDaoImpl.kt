package com.michael.features.favorite

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class FavoriteDaoImpl : FavoriteDao {
    override suspend fun getAll(): List<Favorite> = dbQuery {
        FavoriteTable.selectAll().map { it.toFavorite() }
    }

    override suspend fun addFavorite(favorite: Favorite): Favorite? = dbQuery {
        val favoriteInsertStatement = FavoriteTable.insert {
            it[userId] = favorite.userId
            it[movieId] = favorite.movieId
            it[addedDate] = favorite.addedDate
        }

        favoriteInsertStatement.resultedValues?.singleOrNull()?.toFavorite()
    }

    private fun ResultRow.toFavorite(): Favorite = Favorite(
        this[FavoriteTable.userId],
        this[FavoriteTable.movieId],
        this[FavoriteTable.addedDate]
    )
}