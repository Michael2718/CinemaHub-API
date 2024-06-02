package com.michael.features.favorite

import com.michael.features.movie.Movie
import com.michael.features.movie.MovieTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class FavoritesDaoImpl : FavoritesDao {
    override suspend fun getAll(): List<Favorite> = dbQuery {
        FavoritesTable.selectAll().map { it.toFavorite() }
    }

    override suspend fun deleteFavorite(userId: Int, movieId: String): Boolean = dbQuery {
        FavoritesTable.deleteWhere { (FavoritesTable.userId eq userId) and (FavoritesTable.movieId eq movieId) } != 0
    }

    override suspend fun deleteAllFavorites(userId: Int): Boolean = dbQuery {
        FavoritesTable.deleteWhere { FavoritesTable.userId eq userId } != 0
    }

    override suspend fun getByUserId(userId: Int): List<FavoriteResponse> = dbQuery {
        FavoritesTable
            .join(
                MovieTable,
                JoinType.INNER,
                onColumn = FavoritesTable.movieId,
                otherColumn = MovieTable.movieId
            )
            .selectAll()
            .where { FavoritesTable.userId eq userId }
            .map { it.toFavoriteResponse() }
    }

    override suspend fun isFavorite(userId: Int, movieId: String): Boolean = FavoritesTable
        .selectAll()
        .where { (FavoritesTable.userId eq userId) and (FavoritesTable.movieId eq movieId) }
        .map { it.toFavorite() }
        .isNotEmpty()

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

    private fun ResultRow.toFavoriteResponse(): FavoriteResponse = FavoriteResponse(
        movie = Movie(
            this[MovieTable.movieId],
            this[MovieTable.title],
            this[MovieTable.releaseDate],
            this[MovieTable.duration],
            this[MovieTable.voteAverage],
            this[MovieTable.voteCount],
            this[MovieTable.plot],
            this[MovieTable.isAdult],
            this[MovieTable.popularity],
            this[MovieTable.price],
            this[MovieTable.primaryImageUrl]
        ),
        addedDate = this[FavoritesTable.addedDate]
    )
}
