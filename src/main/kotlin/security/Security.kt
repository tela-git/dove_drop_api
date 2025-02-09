package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Application.configureSecurity(tokenConfig: TokenConfig) {
    install(Authentication) {
        jwt {
            realm =
                this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(tokenConfig.secret))
                    .withAudience(tokenConfig.audience)
                    .withIssuer(tokenConfig.issuer)
                    .build()
            )

            validate { jwtCredential ->
                if(jwtCredential.payload.audience.contains(tokenConfig.audience)) JWTPrincipal(jwtCredential.payload) else null
            }
        }
    }
}
