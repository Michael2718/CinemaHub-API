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
import java.util.concurrent.ConcurrentHashMap

object DatabaseSingleton {
    private lateinit var config: ApplicationConfig
    private val dataSourceMap = ConcurrentHashMap<Pair<String, String>, HikariDataSource>()
    private val lastAccessMap = ConcurrentHashMap<Pair<String, String>, Long>()
    private var idleTimeout: Long = 10000
    private var cleanupInterval: Long = 30000
    private lateinit var cleanupJob: Job

    fun init(config: ApplicationConfig, coroutineScope: CoroutineScope) {
        this.config = config

        idleTimeout = config.property("hikari.idleTimeout").getString().toLong()
        cleanupInterval = config.property("hikari.cleanupInterval").getString().toLong()

        startCleanupTask(coroutineScope)
    }

    fun connectHikari(credentials: Credentials): Database =
        Database.connect(createHikariDataSource(credentials))

    private fun createHikariDataSource(credentials: Credentials): HikariDataSource {
        val key = credentials.toPair()
        lastAccessMap[key] = System.currentTimeMillis()
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
        cleanupJob = coroutineScope.launch {
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

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun Application.configureDatabases() {
    DatabaseSingleton.init(environment.config, this)
}
