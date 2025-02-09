package com.example.domain.model.security

data class SaltedHash(
    val salt: String,
    val hash: String
)
