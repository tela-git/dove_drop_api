package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginCred(
    val email: String = "",
    val password: String = ""
)
