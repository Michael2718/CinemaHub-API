package com.michael.plugins

import com.michael.features.favorite.Favorite
import com.michael.features.favorite.FavoritesDaoImpl
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
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureRouting() {
    routing {
        loginRoute()
        authenticate("auth-jwt") {
            favoritesRoute()
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

fun Route.favoritesRoute() {
    route("/favorites") {
        val dao = FavoritesDaoImpl()

        get("{user_id}") {
            val userId = call.parameters["user_id"]?.toIntOrNull()

            if (userId == null) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val favorites = dao.getByUserId(userId)

            when {
                favorites.isEmpty() -> call.respondText(
                    "Favorite list is empty for user $userId",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, favorites)
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

        route("{user_id}") {
            get {
                val userId = call.parameters["user_id"]?.toIntOrNull()

                if (userId == null) {
                    call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val favorites = dao.getByUserId(userId)

                call.respond(HttpStatusCode.OK, favorites)
            }

            delete {
                val userId = call.parameters["user_id"]?.toIntOrNull()

                if (userId == null) {
                    call.respondText("Missing or invalid userId", status = HttpStatusCode.BadRequest)
                    return@delete
                }

                val isDeleted = dao.deleteAllFavorites(userId)

                if (isDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respondText(
                        "No such user",
                        status = HttpStatusCode.BadRequest
                    )
                }
            }

            delete("{movie_id}") {
                val userId = call.parameters["user_id"]?.toIntOrNull()
                val movieId = call.parameters["movie_id"]

                if (userId == null) {
                    call.respondText("Missing or invalid userId", status = HttpStatusCode.BadRequest)
                    return@delete
                }

                if (movieId == null) {
                    call.respondText("Missing or invalid movieId", status = HttpStatusCode.BadRequest)
                    return@delete
                }

                val isDeleted = dao.deleteFavorite(userId = userId, movieId = movieId)

                if (isDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respondText(
                        "No such user or movie",
                        status = HttpStatusCode.BadRequest
                    )
                }
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
//        get {
//            val historyMovies = dao.getAll()
//            if (historyMovies.isNotEmpty()) {
//                call.respond(HttpStatusCode.OK, historyMovies)
//            } else {
//                call.respond(HttpStatusCode.NoContent)
//            }
//        }

        get("{user_id}") {
            val userId = call.parameters["user_id"]?.toIntOrNull()

            if (userId == null) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val history = dao.getByUserId(userId)

            when {
                history.isEmpty() -> call.respondText(
                    "History list is empty for user $userId",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, history)
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
//        get {
//            val movies = dao.getAll()
//            if (movies.isNotEmpty()) {
////                call.respond(HttpStatusCode.OK, movies)
//                call.respond(HttpStatusCode.OK, mapOf("items" to movies))
//            } else {
//                call.respond(HttpStatusCode.NoContent)
//            }
//        }

        get("{movie_id}") {
            val movieId = call.parameters["movie_id"]

            if (movieId.isNullOrEmpty()) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val movie = dao.getByMovieId(movieId)
            when {
                movie == null -> call.respondText(
                    "Movie not found",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, movie)
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
//        get {
//            val users = dao.getAll()
//            if (users.isNotEmpty()) {
//                call.respond(HttpStatusCode.OK, users)
//            } else {
//                call.respond(HttpStatusCode.NoContent)
//            }
//        }
        get("{user_id}") {
            val userId = call.parameters["user_id"]?.toIntOrNull()

            if (userId == null) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val user = dao.getByUserId(userId)
            when {
                user == null -> call.respondText(
                    "User not found",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, user)
            }
        }

        get {
            val username = call.request.queryParameters["username"] ?: return@get call.respondText(
                "Missing username",
                status = HttpStatusCode.BadRequest
            )

            val user = dao.getByUsername(username) ?: return@get call.respondText(
                "User not found",
                status = HttpStatusCode.BadRequest
            )

            call.respond(
                status = HttpStatusCode.OK,
                user
            )
        }
    }
}
