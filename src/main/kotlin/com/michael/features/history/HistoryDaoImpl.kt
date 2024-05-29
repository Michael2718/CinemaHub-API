package com.michael.features.history

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class HistoryDaoImpl : HistoryDao {
    override suspend fun getAll(): List<History> = dbQuery {
        HistoryTable.selectAll().map { it.toHistory() }
    }

    override suspend fun getByUserId(userId: Int): List<History> = dbQuery {
        HistoryTable
            .selectAll()
            .where { HistoryTable.userId eq userId }
            .map { it.toHistory() }
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
}