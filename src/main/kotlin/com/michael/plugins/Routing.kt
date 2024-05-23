package com.michael.plugins

import com.michael.features.favorite.Favorite
import com.michael.features.favorite.FavoriteDaoImpl
import com.michael.features.genre.GenreDaoImpl
import com.michael.features.history.History
import com.michael.features.history.HistoryDaoImpl
import com.michael.features.movie.Movie
import com.michael.features.movie.MovieDaoImpl
import com.michael.features.review.ReviewDaoImpl
import com.michael.features.transaction.TransactionDaoImpl
import com.michael.features.user.UserDaoImpl
import com.michael.plugins.authentication.Credentials
import com.michael.plugins.authentication.JwtConfig
import com.michael.plugins.authentication.isValidUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureRouting() {
    routing {
        loginRoute()
        authenticate("auth-jwt") {
            favoriteRoute()
            genreTable()
            historyRoute()
            movieRoute()
            reviewRoute()
            transactionRoute()
            userRoute()
        }
    }
}

fun Route.loginRoute() {
    post("/login") {
        val credentials = call.receive<Credentials>()

        if (isValidUser(credentials)) {
            val token = JwtConfig.generateToken(credentials)
            call.respond(mapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }
}

fun Route.favoriteRoute() {
    route("/favorite") {
        val dao = FavoriteDaoImpl()
        get {
            val favoriteMovies = dao.getAll()
            if (favoriteMovies.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, favoriteMovies)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        post {
            try {
                val favoriteMovie = call.receive<Favorite>()
                dao.addFavorite(favoriteMovie)
                call.respond(HttpStatusCode.OK)
            } catch (e: ExposedSQLException) {
                call.respondText("No movie or user (TODO)", status = HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.genreTable() {
    route("genre") {
        val dao = GenreDaoImpl()
        get {
            val genres = dao.getAll()
            if (genres.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, genres)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun Route.historyRoute() {
    route("/history") {
        val dao = HistoryDaoImpl()
        get {
            val historyMovies = dao.getAll()
            if (historyMovies.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, historyMovies)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        post {
            try {
                val historyMovie = call.receive<History>()
                dao.addHistory(historyMovie)
                call.respond(HttpStatusCode.OK)
            } catch (e: ExposedSQLException) {
                call.respondText("No movie or user (TODO)", status = HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.movieRoute() {
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
        post {
            try {
                val movie = call.receive<Movie>()
                dao.addMovie(movie)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.reviewRoute() {
    route("/review") {
        val dao = ReviewDaoImpl()
        get {
            val reviews = dao.getAll()
            if (reviews.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, reviews)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun Route.transactionRoute() {
    route("/transaction") {
        val dao = TransactionDaoImpl()
        get {
            val transactions = dao.getAll()
            if (transactions.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, transactions)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun Route.userRoute() {
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
}
