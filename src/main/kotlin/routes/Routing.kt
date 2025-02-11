package com.example.routes

import com.example.data.auth.OTPService
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.email.EmailRepository
import com.mongodb.client.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.*
import org.koin.ktor.ext.get

fun Application.configureRouting(
    authenticationRepo: AuthenticationRepo,
    otpService: OTPService
) {
    routing {
        //Authentication routes
        authRoutes(
            authenticationRepo = authenticationRepo,
            otpService = otpService
        )

        //authenticated routes
        authenticate()
        getUserId()

        // Static plugin
        staticResources("/static", "static")
    }
}
