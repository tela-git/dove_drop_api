package com.example.data.model.chat

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

data class ChatRoom(
    @BsonId val id: ObjectId,
    val participants: List<String>,
    val chatRoomType: ChatRoomType,
    val createdAt: Instant,
    val lastMessage: ChatMessage?
)