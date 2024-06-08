package com.michael.features.history

import com.michael.features.movie.Movie
import com.michael.features.movie.MoviesTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*

class HistoryDaoImpl : HistoryDao {
    override suspend fun getAll(): List<History> = dbQuery {
        HistoryTable.selectAll().map { it.toHistory() }
    }

    override suspend fun getByUserId(userId: Int): List<HistoryResponse> = dbQuery {
        HistoryTable
            .join(
                MoviesTable,
                JoinType.INNER,
                onColumn = HistoryTable.movieId,
                otherColumn = MoviesTable.movieId
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
            this[MoviesTable.movieId],
            this[MoviesTable.title],
            this[MoviesTable.releaseDate],
            this[MoviesTable.duration],
            this[MoviesTable.voteAverage],
            this[MoviesTable.voteCount],
            this[MoviesTable.plot],
            this[MoviesTable.isAdult],
            this[MoviesTable.popularity],
            this[MoviesTable.price],
            this[MoviesTable.primaryImageUrl]
        ),
        watchedDate = this[HistoryTable.watchedDate],
        watchedDuration = this[HistoryTable.watchedDuration]
    )
}