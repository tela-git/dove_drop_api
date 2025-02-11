package com.example.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class OTPReceivable(
    val email: String,
    val otp: String
)
