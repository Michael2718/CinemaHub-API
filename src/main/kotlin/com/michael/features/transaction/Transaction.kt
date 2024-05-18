package com.michael.features.transaction

import com.michael.utils.todayDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

@Serializable
data class Transaction(
    val transactionId: Int,
    val userId: Int,
    val movieId: Int,
    val purchaseDate: LocalDateTime = todayDateTime(),
    val paymentMethod: Int
)

object TransactionTable : Table("transaction") {
    val transactionId = integer("transaction_id").autoIncrement()
    val userId = integer("user_id")
    val movieId = integer("movie_id")
    val purchaseDate = datetime("purchase_date")
    val paymentMethod = integer("payment_method")

    override val primaryKey = PrimaryKey(transactionId)
}
