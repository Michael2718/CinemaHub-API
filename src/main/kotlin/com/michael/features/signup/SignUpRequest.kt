package com.michael.features.signup

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val password: String
)
