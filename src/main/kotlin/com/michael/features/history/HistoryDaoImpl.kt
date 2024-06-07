package com.michael.features.history

import com.michael.features.movie.Movie
import com.michael.features.movie.MovieTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*

class HistoryDaoImpl : HistoryDao {
    override suspend fun getAll(): List<History> = dbQuery {
        HistoryTable.selectAll().map { it.toHistory() }
    }

    override suspend fun getByUserId(userId: Int): List<HistoryResponse> = dbQuery {
        HistoryTable
            .join(
                MovieTable,
                JoinType.INNER,
                onColumn = HistoryTable.movieId,
                otherColumn = MovieTable.movieId
            )
            .selectAll()
            .where { HistoryTable.userId eq userId }
            .orderBy(HistoryTable.watchedDate, SortOrder.DESC)
            .map { it.toHistoryResponse() }
    }

    override suspend fun addHistory(history: History): History? = dbQuery {
        val historyInsertStatement = HistoryTable.insert {
            it[userId] = history.userId
            it[movieId] = history.movieId
            it[watchedDate] = history.watchedDate
            it[watchedDuration] = history.watchedDuration
        }

        historyInsertStatement.resultedValues?.singleOrNull()?.toHistory()

    }

    private fun ResultRow.toHistory(): History = History(
        this[HistoryTable.userId],
        this[HistoryTable.movieId],
        this[HistoryTable.watchedDate],
        this[HistoryTable.watchedDuration]
    )

    private fun ResultRow.toHistoryResponse(): HistoryResponse = HistoryResponse(
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
        watchedDate = this[HistoryTable.watchedDate],
        watchedDuration = this[HistoryTable.watchedDuration]
    )
}