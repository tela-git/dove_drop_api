package com.example.data.model.auth

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class OTP(
    @BsonId val id: ObjectId,
    val otp: String,
    val email: String,
    val expiresAt: Long
)
