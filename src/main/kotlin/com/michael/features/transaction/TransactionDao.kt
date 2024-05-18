package com.michael.features.transaction


interface TransactionDao {
    suspend fun getAll(): List<Transaction>

    suspend fun addTransaction(
        transaction: Transaction
    ): Transaction?
}
