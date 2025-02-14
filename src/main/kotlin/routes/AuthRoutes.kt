package com.example.routes

import com.example.data.auth.OTPService
import com.example.data.model.auth.*
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.model.network.BaseResponse
import com.example.utils.emailRegex
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    authenticationRepo: AuthenticationRepo,
    otpService: OTPService
) {
    post("/auth/signup") {
        val signupCred: SignupCred = runCatching<SignupCred?> { call.receiveNullable<SignupCred>() }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
            return@post
        }
        when(val response = authenticationRepo.signupUser(signupCred)) {
            is BaseResponse.Failure -> {
                val logThis = response.errorMessage
                call.respond(HttpStatusCode(response.errorInt ?: 400, ""), response.errorMessage)
                return@post
            }
            is BaseResponse.Success<*> -> {
                call.respond(HttpStatusCode.OK, "OTP sent to your your email address")
                return@post
            }
        }
    }


    post("/auth/login") {
        val loginCred: LoginCred = runCatching<LoginCred?> { call.receiveNullable<LoginCred>() }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
            return@post
        }
        when(val response = authenticationRepo.loginUser(loginCred)) {
            is BaseResponse.Failure -> {
                val logging = response.errorMessage
                when(response.errorInt) {
                    404 ->  call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
                    401 ->  call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
                    403 -> call.respond(HttpStatusCode.Forbidden, "Verify your email first.")
                    else ->  call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
                }
            }
            is BaseResponse.Success<LoginSuccessResponse> -> {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("token" to response.data.token)
                )
            }
        }
    }

    post("/auth/verify-email") {
        val otpReceived: OTPReceivable = runCatching<OTPReceivable?> { call.receiveNullable<OTPReceivable>() }
            .getOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Enter 6 digit OTP correctly.")
            return@post
        }
        if(otpReceived.otp.length == 6 && otpReceived.otp.all { it.isDigit() } ) {
            val verified = authenticationRepo.verifyEmail(
                otpReceivable = otpReceived
            )
            if(verified) {
                val added = authenticationRepo.updateUserToVerified(email = otpReceived.email)
                if(added) call.respond(HttpStatusCode.OK, "Your email is verified.")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid OTP entered.")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Enter 6 digit OTP correctly.")
            return@post
        }

    }

    post("/auth/reset-password") {
        val resetPasswordData: ResetPasswordData = runCatching { call.receiveNullable<ResetPasswordData>() }
            .getOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Enter data in a valid format!")
            return@post
        }
        if(resetPasswordData.otp.length == 6 && resetPasswordData.otp.all { it.isDigit() } ) {
            when(val hasResetResponse = authenticationRepo.resetPassword(resetPasswordData)) {
                is BaseResponse.Success -> {
                    call.respond(HttpStatusCode.OK, hasResetResponse.message)
                }
                is BaseResponse.Failure -> {
                    call.respond(HttpStatusCode.BadRequest, hasResetResponse.errorMessage)
                }
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Enter 6 digit OTP correctly.")
            return@post
        }

    }
    get("/auth/forgot-password") {
        val email = runCatching { call.queryParameters["email"] }
            .getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Enter email in a valid format!")
            return@get
        }
        if(!email.matches(emailRegex)) {
            call.respond(HttpStatusCode.BadRequest, "Enter email in a valid format!")
            return@get
        }

        val sent = otpService.sendOTPForVerification(toEmail = email)
        if(sent) {
            call.respond(HttpStatusCode.OK, "OTP send to your email: $email")
            return@get
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable, "Error sending OTP!, please try after some time.")
            return@get
        }
    }

    get("/auth/resend-otp") {
        val email = runCatching { call.queryParameters["email"] }
            .getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Email is required to get OTP!")
            return@get
        }
        if(!email.matches(emailRegex)) {
            call.respond(HttpStatusCode.BadRequest, "Enter email in a valid format!")
            return@get
        }
        val sent = otpService.sendOTPForVerification(toEmail = email)
        if(sent) {
            call.respond(HttpStatusCode.OK, "OTP send to your email: $email")
            return@get
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable, "Error sending OTP!, please try after some time.")
            return@get
        }
    }
}

fun Route.authenticate() {
    authenticate {
        get("/authenticate") {
            call.respond(HttpStatusCode.OK, "Authenticated successfully.")
        }
    }
}

fun Route.getUserId() {
    authenticate {
        get("secret/userId") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim(name = "userId", clazz = String::class)
            call.respond(
                HttpStatusCode.OK,
                mapOf("userId" to userId)
            )
        }
        get("secret/userEmail") {
            val principal = call.principal<JWTPrincipal>()
            val userEmail = principal?.getClaim(name = "userEmail", clazz = String::class)
            call.respond(
                HttpStatusCode.OK,
                mapOf("userEmail" to userEmail)
            )
        }
    }
}