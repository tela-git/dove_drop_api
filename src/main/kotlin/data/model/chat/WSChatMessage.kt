package com.example.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class WSChatMessage(
    val sender: String,
    val textMessage: String
)
