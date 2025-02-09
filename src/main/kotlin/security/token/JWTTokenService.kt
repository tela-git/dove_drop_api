package com.example.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.security.TokenService
import java.util.Date

class JWTTokenService() : TokenService {
    override fun generateToken(tokenConfig: TokenConfig, vararg tokenClaims: TokenClaim): String {
        var token =  JWT.create()
            .withIssuer(tokenConfig.issuer)
            .withAudience(tokenConfig.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenConfig.expiresIn))

        tokenClaims.forEach { claim->
            token.withClaim(claim.name, claim.value)
        }
        return token.sign(Algorithm.HMAC256(tokenConfig.secret))
    }
}