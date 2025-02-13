package com.example.routes

import com.example.data.model.chat.ChatMessage
import com.example.data.model.chat.MessageStatus
import com.example.data.model.chat.WSChatMessage
import com.example.domain.chat.ChatRepository
import com.example.domain.model.network.BaseResponse
import com.example.utils.isHexString
import com.example.utils.stringToObjectId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
            //Email of one participant is fetched from the token
            val p1 = call.principal<JWTPrincipal>()?.getClaim("userEmail", String::class)
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

        val messageResponseFlows: MutableMap<String, MutableSharedFlow<ChatMessage>> = mutableMapOf()
        val activeSessions: MutableMap<String, MutableList<WebSocketSession>> = mutableMapOf()

        webSocket("/chat/{chatRoomId}") {
            val chatRoomId = call.parameters["chatRoomId"]
            //Return if the chatRoomId is null
            if(chatRoomId == null) {
                call.respond(HttpStatusCode.BadRequest, "ChatRoomId is required!")
                return@webSocket
            }
            //Return if the chatRoomId is not an hexaDecimal or if the length is not 24 chars
            if(!isHexString(chatRoomId) || chatRoomId.length != 24) {
                call.respond(HttpStatusCode.BadRequest, "Invalid format of chatRoomId!")
                return@webSocket
            }
            //Get the senderEmail from token and return if email is null
            val senderEmail = call.principal<JWTPrincipal>()?.getClaim("userEmail", String::class)
            if(senderEmail == null) {
                call.respond(HttpStatusCode.Unauthorized, "Access denied!")
                return@webSocket
            }
            //Get the timeStamp from the query parameters and return if they are null
            val timeStamp = call.request.queryParameters["timeStamp"]?.toLongOrNull()
            if(timeStamp == null) {
                call.respond(HttpStatusCode.BadRequest, "TimeStamp is required!")
                return@webSocket
            }

            //Add the session to the active sessions for this chat room
            activeSessions.getOrPut(chatRoomId) { mutableListOf() }.add(this)

            //Get or create the shared flow for this chat room
            val messageResponseFlow = messageResponseFlows.getOrPut(chatRoomId) { MutableSharedFlow() }
            val sharedFlow = messageResponseFlow.asSharedFlow()

            try {
                //Broadcast the messages
                val job = launch {
                    sharedFlow.collect { message->
                        val sessions = activeSessions[chatRoomId]
                        sessions?.forEach { session->
                            if(session != this@webSocket) {
                                session.send(
                                    """
                                    {
                                    sender: "${message.sender}",                             
                                    message: "${message.text}"
                                    }
                                """.trimIndent()
                                )
                            }
                        }
                    }
                }

                runCatching {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val messageContent = frame.readText()

                            val message = ChatMessage(
                                sender = senderEmail,
                                id = ObjectId(),
                                chatRoomId = ObjectId(chatRoomId),
                                text = messageContent,
                                timeStamp = timeStamp,
                                status = MessageStatus.NONE
                            )
                            val insertionResponse = chatRepository.addMessage(message, senderEmail)
                            when (insertionResponse) {
                                is BaseResponse.Failure -> {
                                    when (insertionResponse.errorInt) {
                                        503 -> call.respond(
                                            HttpStatusCode.ServiceUnavailable,
                                            insertionResponse.errorMessage
                                        )

                                        401 -> {
                                            call.respond(HttpStatusCode.Unauthorized, "Permission denied!.")
                                            return@webSocket
                                        }

                                        400 -> {
                                            call.respond(HttpStatusCode.BadRequest, "Invalid chatRoomId")
                                            return@webSocket
                                        }
                                    }
                                }

                                is BaseResponse.Success<String> -> {
                                    chatRepository.updateMessageStatus(
                                        ObjectId(insertionResponse.data),
                                        MessageStatus.UPLOADED
                                    )
                                    call.respond(HttpStatusCode.OK, "Message uploaded.")
                                    //Emit the message to be collected for broadcast
                                    messageResponseFlow.emit(message)
                                }
                            }
                        }
                    }
                }
                    .onFailure { exception: Throwable ->
                        println(exception.localizedMessage)
                        //Remove the session from the active chat sessions
                        activeSessions[chatRoomId]?.remove(this)
                    }.also { job.cancel() }

            } catch (e: ClosedReceiveChannelException) {
                call.respond(HttpStatusCode.ServiceUnavailable, "websocket closed")
            } finally {
                // Remove the session when the connection is closed
                activeSessions[chatRoomId]?.remove(this)
            }
        }

        get("/messageDelivered") {
            val messageIdString = call.request.queryParameters["messageId"]
            if(stringToObjectId(messageIdString) == null) {
                call.respond(HttpStatusCode.BadRequest, "Message id is required! and should be 24 hex char long!")
                return@get
            }
            val updated = chatRepository.updateMessageStatus(
                messageId = ObjectId(messageIdString),
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