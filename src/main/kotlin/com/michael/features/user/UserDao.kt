package com.michael.features.user

interface UserDao {
    suspend fun getAll(): List<User>
}