package com.michael.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        signInRoute()
        signUpRoute()
        authenticate("user-auth-jwt", "admin-auth-jwt") {
            searchRoute()
            favoritesRoute()
            genresRoute()
            historyRoute()
            moviesRoute()
            reviewsRoute()
            transactionsRoute()
            usersRoute()
        }
        authenticate("admin-auth-jwt") {
            moviesRouteAdmin()
            usersRouteAdmin()
            reviewsRouteAdmin()
            statisticsRouteAdmin()
        }
    }
}
