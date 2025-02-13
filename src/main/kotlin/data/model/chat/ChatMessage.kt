package com.example.data.model.chat

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ChatMessage(
    @BsonId val id: ObjectId,
    val chatRoomId: ObjectId,
    val text: String,
    val sender: String,
    val timeStamp: Long,
    val status: MessageStatus
)
