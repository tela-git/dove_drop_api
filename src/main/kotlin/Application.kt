package com.example

import com.example.data.auth.AuthenticationRepoImpl
import com.example.data.database.remote.configureDatabases
import com.example.data.database.remote.connectToMongoDB
import com.example.routes.configureRouting
import com.example.security.configureSecurity
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JWTTokenService
import com.example.security.token.TokenConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDatabase = connectToMongoDB()
    val sha256HashingService = SHA256HashingService()
    val tokenService = JWTTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        secret = System.getenv("JWTSECRET"),
        expiresIn = 1000L * 60L * 60L * 24L * 30L // for one month
    )

    val authenticationRepo = AuthenticationRepoImpl(
        database = mongoDatabase,
        hashingService = sha256HashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig
    )

    configureSecurity(
        tokenConfig = tokenConfig
    )
    configureSerialization()
    configureDatabases()
    //configureFrameworks()
    configureRouting(
        authenticationRepo = authenticationRepo
    )
}
