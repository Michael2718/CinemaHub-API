package com.michael.features.review

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Review(
    val userId: Int,
    val movieId: Int,
    val vote: Int,
    val comment: String,
    val likes: Int,
    val dislikes: Int,
)

object ReviewTable : Table("review") {
    val userId = integer("user_id")
    val movieId = integer("movie_id")
    val vote = integer("vote")
    val comment = varchar("comment", 256)
    val likes = integer("likes")
    val dislikes = integer("dislikes")

    override val primaryKey = PrimaryKey(userId, movieId)
}
