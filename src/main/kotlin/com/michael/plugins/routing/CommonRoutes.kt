package com.michael.plugins.routing

import com.michael.features.favorite.Favorite
import com.michael.features.favorite.FavoriteRequest
import com.michael.features.favorite.FavoritesDaoImpl
import com.michael.features.genre.GenreDaoImpl
import com.michael.features.history.History
import com.michael.features.history.HistoryDaoImpl
import com.michael.features.movie.MovieDaoImpl
import com.michael.features.review.ReviewDaoImpl
import com.michael.features.search.SearchDaoImpl
import com.michael.features.signup.SignUpRequest
import com.michael.features.signup.createPostgresUser
import com.michael.features.signup.insertUser
import com.michael.features.transaction.TransactionDaoImpl
import com.michael.features.user.UpdateUserRequest
import com.michael.features.user.UserDaoImpl
import com.michael.plugins.DatabaseSingleton
import com.michael.plugins.authentication.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.postgresql.util.PGInterval
import org.postgresql.util.PGmoney

fun Route.signInRoute() {
    post("/signin") {
        val credentials = call.receive<Credentials>()

        if (isValidUser(credentials)) {
            val isAdmin = isAdmin(credentials)
            val token = JwtConfig.generateToken(credentials, isAdmin)
            call.respond(
                HttpStatusCode.OK,
                token
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }
}

fun Route.signUpRoute() {
    post("signup") {
        try {
            val request = call.receive<SignUpRequest>()
            DatabaseSingleton.connectHikari()
            if (!insertUser(request)) {
                call.respondText(
                    "Invalid user info",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            val credentials = createPostgresUser(request.username, request.password)
            if (credentials == null) {
                call.respondText(
                    "User role was not created",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            val token = JwtConfig.generateToken(credentials, isAdmin = false)
            call.respond(token)
        } catch (e: Exception) {
            call.respondText(
                "Something went wrong",
                status = HttpStatusCode.BadRequest
            )
        }
    }
}

fun Route.searchRoute() {
    route("/search") {
        val dao = SearchDaoImpl()

        get {
            val query = call.request.queryParameters["query"]
            val minVoteAverage = call.request.queryParameters["minVoteAverage"]?.toDoubleOrNull()
            val maxVoteAverage = call.request.queryParameters["maxVoteAverage"]?.toDoubleOrNull()
            val minReleaseDate = call.request.queryParameters["minReleaseDate"]?.let { LocalDate.parse(it) }
            val maxReleaseDate = call.request.queryParameters["maxReleaseDate"]?.let { LocalDate.parse(it) }
            val minDuration = call.request.queryParameters["minDuration"]?.let { PGInterval(it) }
            val maxDuration = call.request.queryParameters["maxDuration"]?.let { PGInterval(it) }
            val minPrice = call.request.queryParameters["minPrice"]?.let { PGmoney(it.toDouble()) }
            val maxPrice = call.request.queryParameters["maxPrice"]?.let { PGmoney(it.toDouble()) }
            val isAdult = call.request.queryParameters["isAdult"]?.toBooleanStrictOrNull()

            val userId = call.request.queryParameters["userId"]?.toIntOrNull()

            if (query == null) {
                call.respondText(
                    "Missing query parameter",
                    status = HttpStatusCode.BadRequest
                )
                return@get
            }

            if (userId == null) {
                call.respondText(
                    "Missing userId parameter",
                    status = HttpStatusCode.BadRequest
                )
                return@get
            }

            val movies = dao.searchMovies(
                query = query,
                minVoteAverage = minVoteAverage,
                maxVoteAverage = maxVoteAverage,
                minReleaseDate = minReleaseDate,
                maxReleaseDate = maxReleaseDate,
                minDuration = minDuration,
                maxDuration = maxDuration,
                minPrice = minPrice,
                maxPrice = maxPrice,
                isAdult = isAdult,
                userId = userId
            )

            call.respond(HttpStatusCode.OK, movies)
        }
    }
}

fun Route.favoritesRoute() {
    route("/favorites") {
        val dao = FavoritesDaoImpl()

        post {
            try {
                val (userId, movieId) = call.receive<FavoriteRequest>()
                dao.addFavorite(
                    Favorite(userId, movieId)
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: ExposedSQLException) {
                call.respondText("No movie or user or movie is already added", status = HttpStatusCode.BadRequest)
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

fun Route.genresRoute() {
    route("genres") {
        val dao = GenreDaoImpl()
        get {
            val genres = dao.getAll()
            if (genres.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, genres)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        get("movies") {
            val genres = dao.getAllMovies()
            if (genres.isNullOrEmpty()) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.OK, genres)
            }
        }

        get("{genreId}") {
            val genreId = call.parameters["genreId"]?.toIntOrNull()

            if (genreId == null) {
                call.respondText("Missing or invalid genreId", status = HttpStatusCode.BadRequest)
                return@get
            }

            val movies = dao.getMoviesByGenreId(genreId)
            when {
                movies.isEmpty() -> call.respondText(
                    "Movies not found",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, movies)
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

fun Route.moviesRoute() {
    route("/movies") {
        val dao = MovieDaoImpl()

        get("{movie_id}") {
            val movieId = call.parameters["movie_id"]

            if (movieId.isNullOrEmpty()) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val movie = dao.get(movieId)
            when {
                movie == null -> call.respondText(
                    "Movie not found",
                    status = HttpStatusCode.BadRequest
                )

                else -> call.respond(HttpStatusCode.OK, movie)
            }
        }

        get("{movieId}/{userId}") {
            try {
                val movieId = call.parameters["movieId"]
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null || movieId == null) {
                    call.respondText("Missing or invalid ids", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val movie = dao.getByUserId(movieId, userId)
                if (movie == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(message = movie, status = HttpStatusCode.OK)
                }
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.reviewsRoute() {
    route("/reviews") {
        val dao = ReviewDaoImpl()
        get {
            val reviews = dao.getAll()
            if (reviews.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, reviews)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        get("{movieId}") {
            val movieId = call.parameters["movieId"]

            if (movieId == null) {
                call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                return@get
            }

            val reviews = dao.getReviews(movieId)

            call.respond(HttpStatusCode.OK, reviews)
        }

        get("{movieId}/{userId}") {
            try {
                val movieId = call.parameters["movieId"]
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null || movieId == null) {
                    call.respondText("Missing or invalid ids or vote", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val review = dao.getReview(movieId, userId)
                if (review == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(message = review, status = HttpStatusCode.OK)
                }
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }

        post("{movieId}/{userId}") {
            try {
                val movieId = call.parameters["movieId"]
                val userId = call.parameters["userId"]?.toIntOrNull()
                val vote = call.request.queryParameters["vote"]?.toIntOrNull()
                val comment = call.request.queryParameters["comment"]
                if (userId == null || movieId == null || vote == null || comment == null) {
                    call.respondText("Missing or invalid ids or vote", status = HttpStatusCode.BadRequest)
                    return@post
                }

                val review = dao.addReview(movieId, userId, vote, comment)
                if (review == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(message = review, status = HttpStatusCode.OK)
                }
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }

        post("{movieId}/{userId}/rate") {
            try {
                val movieId = call.parameters["movieId"]
                val userId = call.parameters["userId"]?.toIntOrNull()
                val like = call.request.queryParameters["like"]?.toBoolean()

                if (userId == null || movieId == null || like == null) {
                    call.respondText("Missing or invalid ids or parameter 'like'", status = HttpStatusCode.BadRequest)
                    return@post
                }
                if (like) {
                    dao.like(movieId, userId)
                } else {
                    dao.dislike(movieId, userId)
                }

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.transactionsRoute() {
    route("/transactions") {
        val dao = TransactionDaoImpl()
        get {
            val transactions = dao.getAll()
            if (transactions.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, transactions)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        post("{movieId}/{userId}/{paymentMethod}") {
            try {
                val movieId = call.parameters["movieId"]
                val userId = call.parameters["userId"]?.toIntOrNull()
                val paymentMethod = call.parameters["paymentMethod"]?.toIntOrNull()
                if (userId == null || movieId == null || paymentMethod == null) {
                    call.respondText("Missing or invalid ids or paymentMethod", status = HttpStatusCode.BadRequest)
                    return@post
                }

                val transaction = dao.addTransaction(userId, movieId, paymentMethod)
                if (transaction == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(message = transaction, status = HttpStatusCode.OK)
                }
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.usersRoute() {
    route("/users") {
        val dao = UserDaoImpl()

        get("{user_id}") {
            val userId = call.parameters["user_id"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing or invalid id",
                status = HttpStatusCode.BadRequest
            )

            val user = dao.getByUserId(userId) ?: call.respondText(
                "User not found",
                status = HttpStatusCode.BadRequest
            )

            call.respond(HttpStatusCode.OK, user)
        }

        get("/username/{username}") {
            val username = call.parameters["username"] ?: return@get call.respondText(
                "Missing username",
                status = HttpStatusCode.BadRequest
            )

            val user = dao.getByUsername(username) ?: return@get call.respondText(
                "User not found",
                status = HttpStatusCode.BadRequest
            )

            call.respond(HttpStatusCode.OK, user)
        }

        put("{user_id}") {
            try {
                val userId = call.parameters["user_id"]?.toIntOrNull() ?: return@put call.respondText(
                    "Missing or invalid id",
                    status = HttpStatusCode.BadRequest
                )

                val request = call.receive<UpdateUserRequest>()
                val updatedUser = dao.updateUser(userId, request) ?: return@put call.respondText(
                    "Invalid user info or invalid id",
                    status = HttpStatusCode.BadRequest
                )

                call.respond(message = updatedUser, status = HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}
