package com.michael.plugins

import com.michael.plugins.authentication.Credentials
import com.michael.plugins.authentication.toPair
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseSingleton {
    private lateinit var config: ApplicationConfig
    private val dataSourceMap = mutableMapOf<Pair<String, String>, HikariDataSource>()


    fun init(config: ApplicationConfig) {
        this.config = config
    }

//    fun connectExposed(username: String, password: String): Database = Database.connect(
//        url = config.property("postgres.url").getString(),
//        driver = config.property("postgres.driver").getString(),
//        user = username,
//        password = password
//    )

    fun connectHikari(credentials: Credentials): Database =
        Database.connect(createHikariDataSource(credentials))

    private fun createHikariDataSource(credentials: Credentials): HikariDataSource =
        dataSourceMap.computeIfAbsent(credentials.toPair()) {
            HikariDataSource(
                HikariConfig().apply {
                    driverClassName = config.property("postgres.driver").getString()
                    jdbcUrl = config.property("postgres.url").getString()
                    maximumPoolSize = 1
                    isAutoCommit = false
                    username = credentials.username
                    password = credentials.password
                    transactionIsolation = "TRANSACTION_READ_COMMITTED"
                    validate()
                }
            )
        }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun Application.configureDatabases() {
    DatabaseSingleton.init(environment.config)
}
