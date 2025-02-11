package com.example.routes

import com.example.data.auth.OTPService
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.chat.ChatRepository
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    authenticationRepo: AuthenticationRepo,
    otpService: OTPService,
    chatRepository: ChatRepository
) {
    routing {
        //Authentication routes
        authRoutes(
            authenticationRepo = authenticationRepo,
            otpService = otpService
        )

        //Chat routes
        chatRoutes(
            chatRepository = chatRepository
        )

        //Authenticated routes
        authenticate()
        getUserId()

        // Static plugin
        staticResources("/static", "static")
    }
}
