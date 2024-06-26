package com.michael.features.statistics

interface StatisticsDao {
    suspend fun getGenresStats(): List<GenresStats>
    suspend fun getUsersStats(): List<UsersStats>
}
