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
    val userRealm = environment.config.property("jwt.user_realm").getString()
    val adminRealm = environment.config.property("jwt.admin_realm").getString()

    JwtConfig.init(environment.config)
    install(Authentication) {
        jwt("user-auth-jwt") {
            realm = userRealm
            verifier(JwtConfig.user_verifier)
            validate { jwtCredential ->
                validateJwt(jwtCredential)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
        jwt("admin-auth-jwt") {
            realm = adminRealm
            verifier(JwtConfig.admin_verifier)
            validate { jwtCredential ->
                validateJwt(jwtCredential)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

fun validateJwt(jwtCredential: JWTCredential): JWTPrincipal? {
    val username = jwtCredential.payload.getClaim("username").asString()
    val password = jwtCredential.payload.getClaim("password").asString()

    if (username.isEmpty() || password.isEmpty()) return null

    return try {
        DatabaseSingleton.connectHikari(Credentials(username, password))
        JWTPrincipal(jwtCredential.payload)
    } catch (e: Exception) {
        null
    }
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

fun isAdmin(credentials: Credentials): Boolean {
    val query = "SELECT EXISTS (SELECT 1\n" +
            "               FROM pg_roles\n" +
            "                        LEFT JOIN pg_auth_members ON pg_roles.oid = pg_auth_members.member\n" +
            "               WHERE rolname = '${credentials.username}'\n" +
            "                 AND roleid = (SELECT oid FROM pg_roles WHERE rolname = 'admin_role'));"
    try {
        val db = DatabaseSingleton.connectHikari(credentials)
        val result = transaction(db) {
            this.exec(query) { resultSet ->
                if (resultSet.next()) {
                    resultSet.getBoolean(1)
                } else {
                    false
                }
            }
        } ?: false
        return result
    } catch (e: Exception) {
        return false
    }
}

@Serializable
data class Credentials(val username: String, val password: String)

fun Credentials.toPair() = this.username to this.password

@Serializable
data class Token(
    val token: String
)

