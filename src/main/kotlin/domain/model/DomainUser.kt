package com.example.domain.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class DomainUser(
    val id: String,
    val fullName: String?,
    val email: String,
    val imageUrl: String?,
    val availability: String,
)
