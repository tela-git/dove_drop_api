package com.example.routes

import com.example.domain.chat.ChatRepository
import com.example.routes.authenticate
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.chatRoutes(
    chatRepository: ChatRepository
) {
    authenticate {
        webSocket("/chats") {
            send("Enter your name: ")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                if (receivedText.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said bye."))
                } else {
                    send(Frame.Text("you entered: $receivedText"))
                }
            }
        }
    }
}