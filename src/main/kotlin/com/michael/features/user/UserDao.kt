package com.michael.features.user

interface UserDao {
    suspend fun getAll(): List<User>

    suspend fun getByUserId(userId: Int): User?

    suspend fun getByUsername(username: String): User?

    suspend fun updateUser(userId: Int, userRequest: UpdateUserRequest): User?
}
