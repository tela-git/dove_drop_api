package com.example.domain.security

import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig

interface TokenService {
    fun generateToken(tokenConfig: TokenConfig, vararg tokenClaims: TokenClaim): String
}