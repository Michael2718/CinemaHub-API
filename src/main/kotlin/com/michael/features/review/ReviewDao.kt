package com.michael.features.review

interface ReviewDao {
    suspend fun getAll(): List<Review>

    suspend fun addReview(
        movieId: String,
        userId: Int,
        vote: Int,
        comment: String
    ): Review?

    suspend fun getReviews(movieId: String): List<ReviewResponse>
    suspend fun getReview(movieId: String, userId: Int): ReviewResponse?

    suspend fun like(movieId: String, userId: Int): Boolean
    suspend fun dislike(movieId: String, userId: Int): Boolean
}
