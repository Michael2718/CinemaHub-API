package com.michael.features.user

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

class UserDaoImpl : UserDao {
    override suspend fun getAll(query: String?): List<User> = dbQuery {
        val conditions = mutableListOf<Op<Boolean>>()
        query?.let {
            conditions.add(
                (UserTable.username.lowerCase() like "%${it.lowercase()}%") or
                        (UserTable.firstName.lowerCase() like "%${it.lowercase()}%") or
                        (UserTable.lastName.lowerCase() like "%${it.lowercase()}%") or
                        (UserTable.email.lowerCase() like "%${it.lowercase()}%")
            )
        }

        UserTable
            .selectAll()
            .where {
                conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }
            }
            .orderBy(UserTable.userId)
            .map { it.toUser() }
    }

    override suspend fun getByUserId(userId: Int): User? = dbQuery {
        val query = UserTable.selectAll().where { UserTable.userId eq userId }

        query.map { it.toUser() }.singleOrNull()
    }

    override suspend fun getByUsername(username: String): User? = dbQuery {
        val query = UserTable.selectAll().where { UserTable.username eq username }

        query.map { it.toUser() }.singleOrNull()
    }

    override suspend fun updateUser(userId: Int, updateRequest: UpdateUserRequest): User? = dbQuery {
        val query = UserTable.update({ UserTable.userId eq userId }) {
            it[username] = updateRequest.username
            it[firstName] = updateRequest.firstName
            it[lastName] = updateRequest.lastName
            it[email] = updateRequest.email
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

    override suspend fun deleteUser(userId: Int): Boolean = dbQuery {
        UserTable.deleteWhere { UserTable.userId eq userId } != 0
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
