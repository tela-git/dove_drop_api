package com.example

import com.example.data.ChatRepositoryImpl
import com.example.data.account.UserAccountRepoImpl
import com.example.data.auth.AuthenticationRepoImpl
import com.example.data.auth.OTPService
import com.example.data.database.remote.configureDatabases
import com.example.data.database.remote.connectToMongoDB
import com.example.data.email.EmailRepositoryImpl
import com.example.domain.email.EmailRepository
import com.example.routes.configureRouting
import com.example.security.configureSecurity
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JWTTokenService
import com.example.security.token.TokenConfig
import io.appwrite.Client
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDatabase = connectToMongoDB()
    val sha256HashingService = SHA256HashingService()
    val tokenService = JWTTokenService()
    val emailRepository = EmailRepositoryImpl()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        secret = System.getenv("JWTSECRET"),
        expiresIn = 1000L * 60L * 60L * 24L // for one day
    )
    val otpService = OTPService(
        emailRepository = emailRepository,
        database = mongoDatabase
    )
    val authenticationRepo = AuthenticationRepoImpl(
        database = mongoDatabase,
        hashingService = sha256HashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig,
        otpService = otpService
    )
    val chatRepository = ChatRepositoryImpl(
        database = mongoDatabase,
        authenticationRepo = authenticationRepo
    )
    val userAccountRepository = UserAccountRepoImpl(
        database = mongoDatabase
    )
    configureSecurity(
        tokenConfig = tokenConfig
    )
    configureSerialization()
    configureDatabases()
    configureWebSockets()
    //configureFrameworks()
    configureRouting(
        authenticationRepo = authenticationRepo,
        otpService = otpService,
        chatRepository = chatRepository,
        userAccountRepository = userAccountRepository
    )
}
