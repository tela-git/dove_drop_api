package com.example.domain.security

import com.example.domain.model.security.SaltedHash

interface HashingService {
    fun generateSaltedHash(value: String, saltLength: Int): SaltedHash
    fun verify(value: String, saltedHash: SaltedHash): Boolean
}