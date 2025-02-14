package com.example.data.model

import com.example.domain.model.DomainUser
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId val id: ObjectId? = null,
    val fullName: String?,
    val email: String,
    val password: String,
    val imageUrl: String?,
    val salt: String,
    val verified: Boolean,
    val availability: String?,
)

fun User.toDomainUser(): DomainUser {
    return DomainUser(
        fullName = fullName,
        email = email,
        imageUrl = imageUrl,
        id = id?.toHexString() ?: "",
        availability = availability ?: ""
    )
}
