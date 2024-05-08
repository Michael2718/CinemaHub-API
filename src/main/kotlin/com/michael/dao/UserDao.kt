package com.michael.dao

import com.michael.models.User

interface UserDao {
    suspend fun getAll(): List<User>
}