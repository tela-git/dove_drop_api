package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SignupCred(
    val name: String,
    val email: String,
    val password: String
)
