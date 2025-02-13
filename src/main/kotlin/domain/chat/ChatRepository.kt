package com.example.domain.chat

import com.example.data.model.chat.ChatMessage
import com.example.data.model.chat.ChatRoom
import com.example.data.model.chat.MessageStatus
import com.example.domain.model.network.BaseResponse
import org.bson.types.ObjectId

interface ChatRepository {
    // returns the inserted id when success
    suspend fun addMessage(message: ChatMessage): BaseResponse<String>

    suspend fun getOrCreateChatRoom(participants: List<String>): BaseResponse<ChatRoom>

    suspend fun updateMessageStatus(messageId: ObjectId, status: MessageStatus): Boolean
}