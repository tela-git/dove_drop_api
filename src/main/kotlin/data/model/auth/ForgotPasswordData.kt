package com.example.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordData(
    val email: String
)
