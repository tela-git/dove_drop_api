package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OTPReceivable(
    val email: String,
    val otp: String
)
