package com.michael.plugins

import com.michael.dao.MovieDao
import com.michael.dao.MovieDaoImpl
import com.michael.dao.UserDaoImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/user") {
            val dao = UserDaoImpl()
            get {
                val users = dao.getAll()
                if (users.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, users)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
        route("/movie") {
            val dao = MovieDaoImpl()
            get {
                val movies = dao.getAll()
                if (movies.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, movies)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
