package com.michael.features.review

import com.michael.features.user.UserTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.JoinType
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

    override suspend fun getByMovieId(movieId: String): List<ReviewResponse> = dbQuery {
        ReviewTable
            .join(
                UserTable,
                JoinType.INNER,
                onColumn = ReviewTable.userId,
                otherColumn = UserTable.userId
            )
            .selectAll()
            .where { ReviewTable.movieId eq movieId }
            .map { it.toReviewResponse() }
    }


}

fun ResultRow.toReview(): Review = Review(
    this[ReviewTable.userId],
    this[ReviewTable.movieId],
    this[ReviewTable.vote],
    this[ReviewTable.comment],
    this[ReviewTable.likes],
    this[ReviewTable.dislikes]
)

fun ResultRow.toReviewResponse(): ReviewResponse = ReviewResponse(
    this[UserTable.username],
    this[ReviewTable.vote],
    this[ReviewTable.comment],
    this[ReviewTable.likes],
    this[ReviewTable.dislikes]
)

