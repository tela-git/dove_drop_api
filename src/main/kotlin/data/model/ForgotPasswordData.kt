package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordData(
    val email: String
)
