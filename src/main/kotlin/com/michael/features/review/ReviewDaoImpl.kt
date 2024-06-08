package com.michael.features.review

import com.michael.features.user.UserTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus

class ReviewDaoImpl : ReviewDao {
    override suspend fun getAll(): List<Review> = dbQuery {
        ReviewTable.selectAll().map { it.toReview() }
    }

    override suspend fun addReview(
        movieId: String,
        userId: Int,
        vote: Int,
        comment: String
    ): Review? = dbQuery {
        val query = ReviewTable.insert {
            it[this.userId] = userId
            it[this.movieId] = movieId
            it[this.vote] = vote
            it[this.comment] = comment
            it[likes] = 0
            it[dislikes] = 0
        }

        query.resultedValues?.singleOrNull()?.toReview()
    }

    override suspend fun getReviews(movieId: String): List<ReviewResponse> = dbQuery {
        ReviewTable
            .join(
                UserTable,
                JoinType.INNER,
                onColumn = ReviewTable.userId,
                otherColumn = UserTable.userId
            )
            .selectAll()
            .where { ReviewTable.movieId eq movieId }
            .orderBy(ReviewTable.likes, order = SortOrder.DESC)
            .map { it.toReviewResponse() }
    }

    override suspend fun getReview(movieId: String, userId: Int): ReviewResponse? = dbQuery {
        ReviewTable
            .join(
                UserTable,
                JoinType.INNER,
                onColumn = ReviewTable.userId,
                otherColumn = UserTable.userId
            )
            .selectAll()
            .where { (ReviewTable.movieId eq movieId) and (ReviewTable.userId eq userId) }
            .singleOrNull()?.toReviewResponse()
    }

    override suspend fun like(movieId: String, userId: Int): Boolean = dbQuery {
        val query = ReviewTable
            .update(
                { (ReviewTable.userId eq userId) and (ReviewTable.movieId eq movieId) }
            ) {
                it[likes] = likes.plus(1)
            }
        query == 1
    }

    override suspend fun dislike(movieId: String, userId: Int): Boolean = dbQuery {
        val query = ReviewTable
            .update(
                { (ReviewTable.userId eq userId) and (ReviewTable.movieId eq movieId) }
            ) {
                it[dislikes] = dislikes.plus(1)
            }
        query == 1
    }

    override suspend fun deleteReview(movieId: String, userId: Int): Boolean = dbQuery {
        ReviewTable.deleteWhere { (ReviewTable.movieId eq movieId) and (ReviewTable.userId eq userId) } != 0
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
    this[ReviewTable.movieId],
    this[ReviewTable.userId],
    this[UserTable.username],
    this[ReviewTable.vote],
    this[ReviewTable.comment],
    this[ReviewTable.likes],
    this[ReviewTable.dislikes]
)

