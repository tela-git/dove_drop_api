package com.example.data

import com.example.data.model.chat.*
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.chat.ChatRepository
import com.example.domain.model.network.BaseResponse
import com.example.utils.toStringX
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.BsonObjectId
import org.bson.types.ObjectId
import java.time.Instant

class ChatRepositoryImpl(
    private val database: MongoDatabase,
    private val authenticationRepo: AuthenticationRepo
): ChatRepository {
    private val chatRoomCollection = database.getCollection<ChatRoom>("chat_rooms")
    private val messageCollection = database.getCollection<ChatMessage>("messages")

    override suspend fun addMessage(message: ChatMessage): BaseResponse<String> {
        val inserted = messageCollection.insertOne(message)
        return if(inserted.wasAcknowledged()) {
            BaseResponse.Success("Insertion successful", inserted.insertedId.toStringX())
        } else {
            BaseResponse.Failure("Error adding message to database")
        }
    }


    override suspend fun getOrCreateChatRoom(participants: List<String>): BaseResponse<ChatRoom> {
        participants.forEach { email->
            authenticationRepo.checkUserExistence(email) ?: return BaseResponse.Failure("Invalid users", 400)
        }
        val chatRoomExisting : ChatRoom? = chatRoomCollection
            .find(
                Filters.or(
                    Filters.eq("participants", participants),
                    Filters.eq("participants", participants.reversed())
                )
            )
            .firstOrNull()
        return if(chatRoomExisting != null) {
            BaseResponse.Success("Retrieved existing chatRoom",chatRoomExisting)
        } else {
            val newChatRoom = ChatRoom(
                id = ObjectId(),
                participants = participants,
                chatRoomType = ChatRoomType.PRIVATE,
                lastMessage = null,
                createdAt = Instant.now()
            )
            val created = chatRoomCollection.insertOne(newChatRoom).wasAcknowledged()
            if(created) BaseResponse.Success("Created new chatRoom",newChatRoom) else BaseResponse.Failure("Error creating new chatRoom")
        }
    }

    override suspend fun updateMessageStatus(messageId: ObjectId, status: MessageStatus): Boolean {
        return messageCollection.updateOne(
            Filters.eq("_id", messageId),
            Updates.set("status", status),
            UpdateOptions().upsert(false)
        ).wasAcknowledged()
    }
}