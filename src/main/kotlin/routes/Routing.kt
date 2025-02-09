package com.example.routes

import com.example.data.model.SignupCred
import com.example.data.model.User
import com.example.domain.auth.AuthenticationRepo
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

fun Application.configureRouting(
    authenticationRepo: AuthenticationRepo
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        //signup route
        signUp(
            authenticationRepo = authenticationRepo
        )

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
