package com.michael.plugins.authentication

import com.michael.plugins.DatabaseSingleton
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureAuthentication() {
//    val secret = environment.config.property("jwt.secret").getString()
//    val issuer = environment.config.property("jwt.issuer").getString()
//    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    JwtConfig.init(environment.config)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JwtConfig.verifier)
            validate { jwtCredential ->
                val username = jwtCredential.payload.getClaim("username").asString()
                val password = jwtCredential.payload.getClaim("password").asString()

                if (username.isEmpty() || password.isEmpty()) return@validate null

                try {
                    DatabaseSingleton.connectHikari(Credentials(username, password))
                } catch (e: Exception) {
                    return@validate null
                }

                JWTPrincipal(jwtCredential.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

@Serializable
data class Credentials(val username: String, val password: String)

fun Credentials.toPair() = this.username to this.password

fun ApplicationCall.getUserCredentials(): Credentials? {
    val principal = this.principal<JWTPrincipal>() ?: return null
    val username = principal.payload.getClaim("username").asString()
    val password = principal.payload.getClaim("password").asString()
    return Credentials(username, password)
}

fun isValidUser(credentials: Credentials): Boolean {
    try {
        val db = DatabaseSingleton.connectHikari(credentials)
        transaction(db) {
            exec("SELECT 1") {}
        }
    } catch (e: Exception) {
        return false
    }
    return true
}
