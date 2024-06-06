package com.michael.features.transaction

import com.michael.utils.todayDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

@Serializable
data class Transaction(
    val userId: Int,
    val movieId: String,
    val purchaseDate: LocalDateTime = todayDateTime(),
    val paymentMethod: Int
)

object TransactionTable : Table("transaction") {
    val userId = integer("user_id")
    val movieId = varchar("movie_id", 10)
    val purchaseDate = datetime("purchase_date")
    val paymentMethod = integer("payment_method")

    override val primaryKey = PrimaryKey(userId, movieId)
}
