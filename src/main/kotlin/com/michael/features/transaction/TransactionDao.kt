package com.michael.features.transaction

import com.michael.utils.todayDateTime
import kotlinx.datetime.LocalDateTime


interface TransactionDao {
    suspend fun getAll(): List<Transaction>

    suspend fun addTransaction(
        userId: Int,
        movieId: String,
        paymentMethod: Int
    ): Transaction?
}
