package com.michael.features.history

interface HistoryDao {
    suspend fun getAll(): List<History>

    suspend fun addHistory(
        history: History
    ): History?
}
