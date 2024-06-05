package com.michael.features.transaction

import com.michael.plugins.DatabaseSingleton.dbQuery
import com.michael.utils.todayDateTime
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TransactionDaoImpl : TransactionDao {
    override suspend fun getAll(): List<Transaction> = dbQuery {
        TransactionTable.selectAll().map { it.toTransaction() }
    }

    override suspend fun addTransaction(
        userId: Int,
        movieId: String,
        paymentMethod: Int
    ): Transaction? = dbQuery {
        val transactionInsertStatement = TransactionTable.insert {
            it[this.userId] = userId
            it[this.movieId] = movieId
            it[this.purchaseDate] = todayDateTime()
            it[this.paymentMethod] = paymentMethod
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