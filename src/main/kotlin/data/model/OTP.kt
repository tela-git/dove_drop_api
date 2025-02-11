package com.example.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

data class OTP(
    @BsonId val id: ObjectId,
    val otp: String,
    val email: String,
    val expiresAt: Long
)
