package com.example.data

import com.example.domain.chat.ChatRepository
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class ChatRepositoryImpl(
    private val database: MongoDatabase
): ChatRepository {
    override suspend fun sendMessage(message: String) {
        TODO("Not yet implemented")
    }
}