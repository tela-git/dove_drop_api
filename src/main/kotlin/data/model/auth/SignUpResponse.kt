package com.example.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResponse<T>(
    val status: String,
    val message: String,
    val data: T?
)
