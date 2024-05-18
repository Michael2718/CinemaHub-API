package com.michael.features.transaction

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TransactionDaoImpl : TransactionDao {
    override suspend fun getAll(): List<Transaction> = dbQuery {
        TransactionTable.selectAll().map { it.toTransaction() }
    }

    override suspend fun addTransaction(transaction: Transaction): Transaction? = dbQuery {
        val transactionInsertStatement = TransactionTable.insert {
            it[transactionId] = transaction.transactionId
            it[userId] = transaction.userId
            it[movieId] = transaction.movieId
            it[purchaseDate] = transaction.purchaseDate
            it[paymentMethod] = transaction.paymentMethod
        }

        transactionInsertStatement.resultedValues?.singleOrNull()?.toTransaction()
    }

    private fun ResultRow.toTransaction(): Transaction = Transaction(
        this[TransactionTable.transactionId],
        this[TransactionTable.userId],
        this[TransactionTable.movieId],
        this[TransactionTable.purchaseDate],
        this[TransactionTable.paymentMethod]
    )
}