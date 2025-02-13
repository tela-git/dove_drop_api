package com.example.routes

import com.example.data.model.auth.SignupCred
import com.example.data.model.chat.ChatMessage
import com.example.data.model.chat.MessageStatus
import com.example.domain.chat.ChatRepository
import com.example.domain.model.network.BaseResponse
import com.example.routes.authenticate
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.bson.types.ObjectId

fun Route.chatRoutes(
    chatRepository: ChatRepository
) {
    authenticate {
        webSocket("/test") {
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

        get("/chatRoomId") {
            val p1 = call.request.queryParameters["participant1"]
            val p2 = call.request.queryParameters["participant2"]
            if(p1 == null || p2 == null) {
                call.respond(HttpStatusCode.BadRequest, "Participants details required to get chatRoomId")
                return@get
            }
            when(val request = chatRepository.getOrCreateChatRoom(listOf(p1,p2))) {
                is BaseResponse.Success -> {
                    val responseBody = """
                        {
                         chatRoomId : ${request.data.id}
                        }
                    """.trimIndent()
                    call.respond(HttpStatusCode.OK,responseBody)
                }
                is BaseResponse.Failure -> {
                    call.respond(HttpStatusCode.BadRequest, request.errorMessage)
                }
            }
        }

        webSocket("/chat/{chatRoomId}") {
            val chatRoomId = call.parameters["chatRoomId"]
            if(chatRoomId == null) {
                call.respond(HttpStatusCode.BadRequest, "ChatRoomId is required!")
                return@webSocket
            }
            val senderEmail = call.request.queryParameters["senderEmail"]
            if(senderEmail == null) {
                call.respond(HttpStatusCode.BadRequest, "SenderId is required!")
                return@webSocket
            }
            val timeStamp = call.request.queryParameters["timeStamp"]?.toLong()
            if(timeStamp == null) {
                call.respond(HttpStatusCode.BadRequest, "TimeStamp is required!")
                return@webSocket
            }
            try {
                for (frame in incoming) {
                    if(frame is Frame.Text) {
                        val messageContent = frame.readText()

                        val message = ChatMessage(
                            sender = senderEmail,
                            id = ObjectId(),
                            chatRoomId = ObjectId(chatRoomId),
                            text = messageContent,
                            timeStamp = timeStamp,
                            status = MessageStatus.NONE
                        )
                        val inserted = chatRepository.addMessage(message)
                        when(inserted) {
                            is BaseResponse.Failure -> {
                                call.respond(HttpStatusCode.ServiceUnavailable, "Message uploading failed!")
                            }
                            is BaseResponse.Success<String> -> {
                                chatRepository.updateMessageStatus(ObjectId(inserted.data), MessageStatus.UPLOADED)
                                call.respond(HttpStatusCode.OK,  "Message uploaded.")
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                call.respond(HttpStatusCode.ServiceUnavailable, "websocket closed")
            }
        }

        get("/messageDelivered") {
            val messageId = call.request.queryParameters["messageId"]
            if(messageId == null) {
                call.respond(HttpStatusCode.BadRequest, "Message id is required!.")
                return@get
            }
            val updated = chatRepository.updateMessageStatus(
                messageId = ObjectId(messageId),
                status = MessageStatus.DELIVERED
            )
            if(updated) {
                call.respond(HttpStatusCode.OK, "Set to delivered")
                return@get
            } else {
                call.respond(HttpStatusCode.BadRequest, "Status update failed")
                return@get
            }
        }
    }
}