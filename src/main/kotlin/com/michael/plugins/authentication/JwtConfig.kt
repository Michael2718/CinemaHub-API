package com.michael.plugins.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var realm: String

    fun init(config: ApplicationConfig) {
        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        realm = config.property("jwt.realm").getString()
    }

    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier
        get() = JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

    fun generateToken(credentials: Credentials): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", credentials.username)
            .withClaim("password", credentials.password)
            .withExpiresAt(Date(System.currentTimeMillis() + 1209600000))
            .sign(algorithm)
    }
}
