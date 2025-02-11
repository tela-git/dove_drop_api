package com.example.domain.chat

interface ChatRepository {
    suspend fun sendMessage(message: String)
}