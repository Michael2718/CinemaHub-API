package com.michael.features.review

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ReviewDaoImpl : ReviewDao {
    override suspend fun getAll(): List<Review> = dbQuery {
        ReviewTable.selectAll().map { it.toReview() }
    }

    override suspend fun addReview(review: Review): Review? = dbQuery {
        val reviewInsertStatement = ReviewTable.insert {
            it[userId] = review.userId
            it[movieId] = review.movieId
            it[vote] = review.vote
            it[comment] = review.comment
            it[likes] = review.likes
            it[dislikes] = review.dislikes
        }

        reviewInsertStatement.resultedValues?.singleOrNull()?.toReview()
    }

    private fun ResultRow.toReview(): Review = Review(
        this[ReviewTable.userId],
        this[ReviewTable.movieId],
        this[ReviewTable.vote],
        this[ReviewTable.comment],
        this[ReviewTable.likes],
        this[ReviewTable.dislikes]
    )
}