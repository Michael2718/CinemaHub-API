package com.michael.features.favorite

import com.michael.utils.todayDate
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

@Serializable
data class Favorite(
    val userId: Int,
    val movieId: String,
    val addedDate: LocalDate = todayDate()
)

object FavoriteTable : Table("favorite") {
    val userId = integer("user_id")
    val movieId = varchar("movie_id", 10)
    val addedDate = date("added_date")

    override val primaryKey = PrimaryKey(userId, movieId)
}
