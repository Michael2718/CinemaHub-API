package com.michael.plugins

import com.michael.plugins.authentication.Credentials
import com.michael.plugins.authentication.toPair
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseSingleton {
    private lateinit var config: ApplicationConfig
    private val dataSourceMap = mutableMapOf<Pair<String, String>, HikariDataSource>()
    private val lastAccessMap = mutableMapOf<Pair<String, String>, Long>()
    private var idleTimeout: Long = 30000
    private var cleanupInterval: Long = 60000
    private lateinit var cleanupJob: Job

    fun init(config: ApplicationConfig, coroutineScope: CoroutineScope) {
        this.config = config

        idleTimeout = config.property("hikari.idleTimeout").getString().toLong()
        cleanupInterval = config.property("hikari.cleanupInterval").getString().toLong()

        startCleanupTask(coroutineScope)
    }

    private fun defaultCredentials() = Credentials(
        username = config.property("postgres.username").getString(),
        password = config.property("postgres.password").getString()
    )

    fun connectHikari(
        credentials: Credentials = defaultCredentials()
    ): Database {
        val ds = createHikariDataSource(credentials)
        return Database.connect(ds)
    }

    private fun createHikariDataSource(credentials: Credentials): HikariDataSource {
        val key = credentials.toPair()
        lastAccessMap[key] = System.currentTimeMillis()
        currentCredentials.set(credentials)
        return dataSourceMap.computeIfAbsent(key) {
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
    }

    private fun startCleanupTask(coroutineScope: CoroutineScope) {
        cleanupJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(cleanupInterval)
                val currentTime = System.currentTimeMillis()
                val keysToRemove = mutableListOf<Pair<String, String>>()
                for ((key, lastAccess) in lastAccessMap) {
                    if (currentTime - lastAccess > idleTimeout) {
                        dataSourceMap[key]?.close()
                        dataSourceMap.remove(key)
                        keysToRemove.add(key)
                    }
                }
                keysToRemove.forEach { lastAccessMap.remove(it) }
            }
        }
    }

    private val currentCredentials = ThreadLocal<Credentials>()

    private fun getCurrentCredentials(): Credentials {
        return currentCredentials.get() ?: throw IllegalStateException("No credentials set in the current context")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val credentials = getCurrentCredentials()
        val database = connectHikari(credentials)
        return newSuspendedTransaction(Dispatchers.IO, database) {
            block()
        }
    }

//    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun Application.configureDatabases() {
    DatabaseSingleton.init(environment.config, this)
}
