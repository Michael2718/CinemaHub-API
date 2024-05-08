package com.michael.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseSingleton {
    fun init(config: ApplicationConfig) {
        val database = Database.connect(
            url = config.property("postgres.url").getString(),
            driver = config.property("postgres.driver").getString(),
            user = config.property("postgres.user").getString(),
            password = config.property("postgres.password").getString()
        )
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun Application.configureDatabases() {
    DatabaseSingleton.init(environment.config)
}
