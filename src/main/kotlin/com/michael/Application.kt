package com.michael

import com.michael.plugins.authentication.configureAuthentication
import com.michael.plugins.configureDatabases
import com.michael.plugins.routing.configureRouting
import com.michael.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureAuthentication()
    configureDatabases()
    configureRouting()
    configureSerialization()
}
