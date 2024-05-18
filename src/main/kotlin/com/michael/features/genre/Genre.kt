package com.michael.features.genre

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Genre(
    val genreId: Int,
    val name: String
)

object GenreTable : Table("genre") {
    val genreId = integer("genre_id")
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(genreId)
}
