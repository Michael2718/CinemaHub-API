package com.michael.features.signup

import com.michael.features.user.UserTable
import com.michael.plugins.DatabaseSingleton.dbQuery
import com.michael.plugins.authentication.Credentials
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun insertUser(signUpRequest: SignUpRequest): Boolean {
    return try {
        dbQuery {
            UserTable.insert {
                it[username] = signUpRequest.username
                it[firstName] = signUpRequest.firstName
                it[lastName] = signUpRequest.lastName
                it[email] = signUpRequest.email
                it[phoneNumber] = signUpRequest.phoneNumber
                it[birthDate] = signUpRequest.birthDate
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

suspend fun createPostgresUser(username: String, password: String): Credentials? {
    return try {
        dbQuery {
            transaction {
                exec("CREATE USER $username WITH PASSWORD '$password'")
                exec("GRANT user_role to $username")
            }
            Credentials(username, password)
        }
    } catch (e: Exception) {
        null
    }
}
