package com.michael.features.user

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class UserDaoImpl : UserDao {
    override suspend fun getAll(): List<User> = dbQuery {
        UserTable.selectAll().map { it.toUser() }
    }

    override suspend fun getByUserId(userId: Int): User? = dbQuery {
        val query = UserTable.selectAll().where { UserTable.userId eq userId }

        query.map { it.toUser() }.singleOrNull()
    }

    override suspend fun getByUsername(username: String): User? = dbQuery {
        val query = UserTable.selectAll().where { UserTable.username eq username }

        query.map { it.toUser() }.singleOrNull()
    }

    override suspend fun updateUser(userId: Int, userRequest: UpdateUserRequest): User? = dbQuery {
        val query = UserTable.update({ UserTable.userId eq userId }) {
            it[username] = userRequest.username
            it[firstName] = userRequest.firstName
            it[lastName] = userRequest.lastName
            it[email] = userRequest.email
        }
        if (query != 1) null
        else {
            UserTable
                .selectAll()
                .where { UserTable.userId eq userId }
                .map { it.toUser() }
                .singleOrNull()
        }
    }
}

fun ResultRow.toUser(): User = User(
    this[UserTable.userId],
    this[UserTable.username],
    this[UserTable.firstName],
    this[UserTable.lastName],
    this[UserTable.email],
    this[UserTable.phoneNumber],
    this[UserTable.birthDate]
)
