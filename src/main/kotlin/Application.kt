package com.example

import com.example.data.auth.AuthenticationRepoImpl
import com.example.data.database.remote.configureDatabases
import com.example.data.database.remote.connectToMongoDB
import com.example.routes.configureRouting
import com.example.security.hashing.SHA256HashingService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDatabase = connectToMongoDB()
    val sha256HashingService = SHA256HashingService()

    val authenticationRepo = AuthenticationRepoImpl(
        database = mongoDatabase,
        hashingService = sha256HashingService
    )

    //configureSecurity()
    configureSerialization()
    configureDatabases()
    //configureFrameworks()
    configureRouting(
        authenticationRepo = authenticationRepo
    )
}
