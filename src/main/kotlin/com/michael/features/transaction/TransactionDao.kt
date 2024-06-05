package com.michael.features.transaction


interface TransactionDao {
    suspend fun getAll(): List<Transaction>

    suspend fun addTransaction(
        userId: Int,
        movieId: String,
        paymentMethod: Int
    ): Transaction?
}
