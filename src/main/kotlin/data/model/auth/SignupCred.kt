package com.example.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignupCred(
    val name: String,
    val email: String,
    val password: String
)
