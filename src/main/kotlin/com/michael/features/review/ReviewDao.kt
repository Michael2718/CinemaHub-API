package com.michael.features.review

interface ReviewDao {
    suspend fun getAll(): List<Review>

    suspend fun addReview(
        review: Review
    ): Review?
}