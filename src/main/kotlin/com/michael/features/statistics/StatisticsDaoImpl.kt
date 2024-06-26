package com.michael.features.statistics

import com.michael.features.favorite.FavoritesTable
import com.michael.features.genre.GenreTable
import com.michael.features.history.HistoryTable
import com.michael.features.history.HistoryTable.watchedDuration
import com.michael.features.movie.MoviesTable
import com.michael.features.movieGenre.MovieGenreTable
import com.michael.features.user.UserTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rowNumber

class StatisticsDaoImpl : StatisticsDao {
    override suspend fun getGenresStats(): List<GenresStats> = dbQuery {
        GenreTable
            .join(
                otherTable = MovieGenreTable,
                joinType = JoinType.LEFT,
                onColumn = GenreTable.genreId,
                otherColumn = MovieGenreTable.genreId
            )
            .join(
                otherTable = MoviesTable,
                joinType = JoinType.LEFT,
                onColumn = MovieGenreTable.movieId,
                otherColumn = MoviesTable.movieId
            )
            .select(
                GenreTable.name,
                MovieGenreTable.movieId.count(),
                MoviesTable.voteCount.sum(),
                MoviesTable.voteAverage.avg()
            )
            .where { MovieGenreTable.movieId.isNotNull() }
            .groupBy(GenreTable.genreId, GenreTable.name)
            .orderBy(GenreTable.name, SortOrder.ASC)
            .map {
                it.toGenresStats()
            }
    }

    override suspend fun getUsersStats(): List<UsersStats> = dbQuery {
        UsersStatsView
            .selectAll()
            .map { it.toUsersStats() }
//        val userWatched = UserTable
//            .join(
//                otherTable = HistoryTable,
//                joinType = JoinType.LEFT,
//                onColumn = UserTable.userId,
//                otherColumn = HistoryTable.userId
//            )
//            .select(
//                UserTable.userId,
//                UserTable.username,
//                UserTable.birthDate,
////                HistoryTable.watchedDuration.sum().alias("watched_hours"), // todo division
//                HistoryTable.movieId.count().alias("watched_movie_count")
//            )
//            .groupBy(UserTable.userId)
//            .alias("UserWatched")
//
//        val favoriteGenre = HistoryTable
//            .join(
//                otherTable = MoviesTable,
//                joinType = JoinType.INNER,
//                onColumn = HistoryTable.movieId,
//                otherColumn = MoviesTable.movieId
//            )
//            .join(
//                otherTable = MovieGenreTable,
//                joinType = JoinType.INNER,
//                onColumn = MoviesTable.movieId,
//                otherColumn = MovieGenreTable.movieId
//            )
//            .join(
//                otherTable = GenreTable,
//                joinType = JoinType.INNER,
//                onColumn = MovieGenreTable.genreId,
//                otherColumn = GenreTable.genreId
//            )
//            .select(
//                HistoryTable.userId,
//                GenreTable.name,
//                HistoryTable.movieId.count(),
//                rowNumber()
//                    .over()
////                    .partitionBy(HistoryTable.movieId.count())
//                    .orderBy(HistoryTable.movieId.count(), SortOrder.DESC)
//                    .alias("rn")
//            )
//            .groupBy(HistoryTable.userId, GenreTable.name)
//            .alias("FavoriteGenre")
//
//        userWatched
//            .join(
//                otherTable = favoriteGenre,
//                joinType = JoinType.LEFT,
//                onColumn = UserTable.userId,
//                otherColumn = HistoryTable.userId
//            )
//            .selectAll()
//            .where {
//                (HistoryTable.movieId.count() neq 0) and (rowNumber().over()
//                    .orderBy(HistoryTable.movieId.count(), SortOrder.DESC) eq 1)
//            }
////            .orderBy(userWatched[sum(History.watchedDuration) / 3600.0], SortOrder.DESC)
//            .map { it.toUsersStats() }
    }
}

fun ResultRow.toGenresStats(): GenresStats = GenresStats(
    this[GenreTable.name],
    this[MovieGenreTable.movieId.count()].toInt(),
    this[MoviesTable.voteCount.sum()] ?: 0,
    this[MoviesTable.voteAverage.avg()]?.toDouble() ?: 0.0
)

fun ResultRow.toUsersStats(): UsersStats = UsersStats(
    this[UsersStatsView.username],
    this[UsersStatsView.birthDate],
    this[UsersStatsView.watchedHours],
    this[UsersStatsView.watchedMovieCount],
    this[UsersStatsView.favoriteGenre]
)
