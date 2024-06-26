package com.michael.plugins.routing

import com.michael.features.movie.AddMovieRequest
import com.michael.features.movie.MovieDaoImpl
import com.michael.features.movie.UpdateMovieRequest
import com.michael.features.review.ReviewDaoImpl
import com.michael.features.statistics.StatisticsDaoImpl
import com.michael.features.user.UserDaoImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.moviesRouteAdmin() {
    route("/movies") {
        val dao = MovieDaoImpl()
        get {
            val queryParams = call.getMovieQueryParams()


            val movies = dao.getAll(
                query  = queryParams.query ?: "",
                minVoteAverage = queryParams.minVoteAverage,
                maxVoteAverage = queryParams.maxVoteAverage,
                minReleaseDate = queryParams.minReleaseDate,
                maxReleaseDate  = queryParams.maxReleaseDate,
                minDuration =  queryParams.minDuration,
                maxDuration = queryParams.maxDuration,
                minPrice = queryParams.minPrice,
                maxPrice = queryParams.maxPrice,
                isAdult = queryParams.isAdult,
            )
            if (movies.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, movies)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        post {
            try {
                val request = call.receive<AddMovieRequest>()
                val movie = dao.addMovie(request) ?: return@post call.respond(HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.OK, movie)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }

        delete("{movieId}") {
            val movieId = call.parameters["movieId"] ?: return@delete call.respondText(
                "Missing or invalid movieId",
                status = HttpStatusCode.BadRequest
            )

            if (dao.deleteMovie(movieId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respondText(
                    "No such movie",
                    status = HttpStatusCode.BadRequest
                )
            }
        }

        put("{movieId}") {
            try {
                val movieId = call.parameters["movieId"]
                if (movieId == null) {
                    call.respondText("Missing or invalid id", status = HttpStatusCode.BadRequest)
                    return@put
                }

                val request = call.receive<UpdateMovieRequest>()
                val updatedMovie = dao.updateMovie(movieId, request)
                if (updatedMovie == null) {
                    call.respondText("Invalid movie info or invalid id", status = HttpStatusCode.BadRequest)
                    return@put
                }
                call.respond(HttpStatusCode.OK, updatedMovie)
            } catch (e: Exception) {
                call.respondText("$e", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Route.usersRouteAdmin() {
    route("/users") {
        val dao = UserDaoImpl()
        get {
            val query = call.request.queryParameters["query"]
            val users = dao.getAll(query)
            if (users.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, users)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        delete("{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull() ?: return@delete call.respondText(
                "Missing or invalid movieId",
                status = HttpStatusCode.BadRequest
            )

            if (dao.deleteUser(userId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respondText(
                    "No such movie",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun Route.reviewsRouteAdmin() {
    route("/reviews") {
        val dao = ReviewDaoImpl()
//        get {
//            val reviews = dao.getAll()
//            if (reviews.isNotEmpty()) {
//                call.respond(HttpStatusCode.OK, reviews)
//            } else {
//                call.respond(HttpStatusCode.NoContent)
//            }
//        }

        delete("{movieId}/{userId}") {
            val movieId = call.parameters["movieId"] ?: return@delete call.respondText(
                "Missing or invalid movieId",
                status = HttpStatusCode.BadRequest
            )

            val userId = call.parameters["userId"]?.toIntOrNull() ?: return@delete call.respondText(
                "Missing or invalid userId",
                status = HttpStatusCode.BadRequest
            )

            if (dao.deleteReview(movieId, userId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respondText(
                    "No such review",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun Route.statisticsRouteAdmin() {
    route("statistics") {
        val dao = StatisticsDaoImpl()

        get("genres") {
            try {
                val stats = dao.getGenresStats()
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.toString())
            }
        }

        get("users") {
            try {
                val stats = dao.getUsersStats()
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.toString())
            }
        }
    }
}
