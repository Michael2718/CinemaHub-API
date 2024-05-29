package com.michael.features.favorite

import com.michael.utils.todayDate
import com.typesafe.config.Optional
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

@Serializable
data class Favorite(
    val userId: Int,
    val movieId: String,
    val addedDate: LocalDate = todayDate()
)

object FavoritesTable : Table("favorite") {
    val userId = integer("user_id")
    val movieId = varchar("movie_id", 10)
    val addedDate = date("added_date")

    override val primaryKey = PrimaryKey(userId, movieId)
}
