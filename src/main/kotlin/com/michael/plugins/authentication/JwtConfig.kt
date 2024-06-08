package com.michael.plugins.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var user_audience: String
    private lateinit var admin_audience: String

    fun init(config: ApplicationConfig) {
        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        user_audience = config.property("jwt.user_audience").getString()
        admin_audience = config.property("jwt.admin_audience").getString()
    }

    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    val user_verifier: JWTVerifier
        get() = JWT
            .require(algorithm)
            .withAudience(user_audience)
            .withIssuer(issuer)
            .build()

    val admin_verifier: JWTVerifier
        get() = JWT
            .require(algorithm)
            .withAudience(admin_audience)
            .withIssuer(issuer)
            .build()


    fun generateToken(credentials: Credentials, isAdmin: Boolean): Token {
        val audience = if (isAdmin) admin_audience else user_audience
        return Token(
            token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", credentials.username)
                .withClaim("password", credentials.password)
                .withExpiresAt(Date(System.currentTimeMillis() + 1209600000))
                .sign(algorithm)
        )
    }
}
