package com.michael.plugins.authentication

import com.michael.plugins.DatabaseSingleton
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
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
                if (jwtCredential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }
            validate { jwtCredential ->
                if (jwtCredential.payload.getClaim("password").asString().isNotEmpty()) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

}

fun isValidUser(username: String, password: String): Boolean {
    try {
        val db = DatabaseSingleton.connect(username, password)
        transaction(db) {
            exec("SELECT 1") {}
        }
    } catch (e: Exception) {
        return false
    }
    return true
}
