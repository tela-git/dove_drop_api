package com.example.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordData(
    val otp: String,
    val email: String,
    val newPassword: String,
)
