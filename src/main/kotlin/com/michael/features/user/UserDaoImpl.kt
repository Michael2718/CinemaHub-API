package com.michael.features.user

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class UserDaoImpl : UserDao {
    override suspend fun getAll(): List<User> = dbQuery {
        UserTable.selectAll().map { it.toUser() }
    }

    private fun ResultRow.toUser(): User = User(
        this[UserTable.userId],
        this[UserTable.username],
        this[UserTable.firstName],
        this[UserTable.lastName],
        this[UserTable.email],
        this[UserTable.phoneNumber],
        this[UserTable.birthDate]
    )
}
