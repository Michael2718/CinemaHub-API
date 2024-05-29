package com.michael.features.user

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

@Serializable
data class User(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber:  String,
    val birthDate: LocalDate,
)

object UserTable : Table("user") {
    val userId = integer("user_id").autoIncrement()
    val username = varchar("username", 30)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 256)
    val phoneNumber = varchar("phone_number", 20)
    val birthDate = date("birth_date")

    override val primaryKey = PrimaryKey(userId)
}