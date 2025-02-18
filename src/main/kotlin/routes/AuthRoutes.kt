package com.example.routes

import com.example.data.auth.OTPService
import com.example.data.model.auth.*
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.model.network.BaseResponse
import com.example.domain.model.network.BaseResult
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
            call.respond(
                HttpStatusCode.BadRequest,
                SignUpResponse<String>(
                    status = "Error",
                    message = SignUpError.InvalidCredFormat.value,
                    data = null
                )
            )
            return@post
        }
        when(val response = authenticationRepo.signupUser(signupCred)) {
            is BaseResponse.Failure -> {
                val logThis = response.errorMessage
                when(response.errorMessage) {
                    SignUpError.UserAlreadyExists.value -> {
                        call.respond(
                            HttpStatusCode.Conflict,
                            SignUpResponse<String>(
                                status = "Error",
                                message = SignUpError.UserAlreadyExists.value,
                                data = null
                            )
                        )
                    }
                    SignUpError.ErrorSendingOTP.value -> {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            SignUpResponse<String>(
                                status = "Error",
                                message = SignUpError.ErrorSendingOTP.value,
                                data = null
                            )
                        )
                    }
                    SignUpError.UnknownError.value -> {
                        call.respond(
                            HttpStatusCode.NotFound,
                            SignUpResponse<String>(
                                status = "Error",
                                message = SignUpError.UnknownError.value,
                                data = null
                            )
                        )
                    }
                }
                return@post
            }
            is BaseResponse.Success<*> -> {
                call.respond(
                    HttpStatusCode.OK,
                    SignUpResponse<String>(
                        status = "Success",
                        message = "OTP successfully sent to your email: ${signupCred.email}",
                        data = null
                    )
                    )
                return@post
            }
        }
    }


    post("/auth/login") {
        val loginCred: LoginCred = runCatching<LoginCred?> { call.receiveNullable<LoginCred>() }.getOrNull() ?: run {
            call.respond(
                HttpStatusCode.BadRequest,mapOf(
                "status" to "Error",
                "message" to "Enter valid details!",
                "data" to null
            ))
            return@post
        }
        when(val response = authenticationRepo.loginUser(loginCred)) {
            is BaseResponse.Failure -> {
                val logging = response.errorMessage
                when(response.errorInt) {
                    404 ->  call.respond(HttpStatusCode.BadRequest,
                        mapOf(
                            "status" to "Error",
                            "message" to "Enter valid details!",
                            "data" to null
                        )
                    )
                    401 ->  call.respond(HttpStatusCode.BadRequest,
                        mapOf(
                            "status" to "Error",
                            "message" to "Enter valid details!",
                            "data" to null
                        )
                    )
                    403 -> call.respond(HttpStatusCode.Forbidden,
                        mapOf(
                            "status" to "Error",
                            "message" to "Please verify your email!",
                            "data" to null
                        )
                    )
                    else ->  call.respond(HttpStatusCode.NotFound,
                        mapOf(
                            "status" to "Error",
                            "message" to "Something went wrong!",
                            "data" to null
                        )
                    )
                }
            }
            is BaseResponse.Success<LoginSuccessResponse> -> {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "Success",
                        "message" to "Login successful.",
                        "data" to response.data.token
                    )
                )
            }
        }
    }

    post("/auth/verify-email") {
        val otpReceived: OTPReceivable = runCatching<OTPReceivable?> { call.receiveNullable<OTPReceivable>() }
            .getOrNull() ?: run {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "status" to "Error",
                        "message" to VerifyEmailError.BadRequest.value
                    )
                )
            return@post
        }

        if(otpReceived.otp.length == 6 && otpReceived.otp.all { it.isDigit() } ) {
            val response = authenticationRepo.verifyEmail(
                otpReceivable = otpReceived
            )
            when(response) {
                is BaseResult.Success -> {
                    val added = authenticationRepo.updateUserToVerified(email = otpReceived.email)
                    if(added) {
                        call.respond(
                            HttpStatusCode.Created,
                            mapOf(
                                "status" to "Success",
                                "message" to "Email Verified Successfully."
                            )
                        )
                        return@post
                    }
                    else {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf(
                                "status" to "Error",
                                "message" to VerifyEmailError.ServerError.value
                            )
                        )
                        println("VerifyEmail: unable to update the user account to verified")
                        return@post
                    }
                }
                is BaseResult.Error<VerifyEmailError> -> {
                    when(response.error) {
                        is VerifyEmailError.InvalidOTP -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf(
                                    "status" to "Error",
                                    "message" to VerifyEmailError.InvalidOTP.value
                                )
                            )
                            return@post
                        }
                        VerifyEmailError.BadRequest -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf(
                                    "status" to "Error",
                                    "message" to VerifyEmailError.BadRequest.value
                                )
                            )
                            return@post
                        }
                        VerifyEmailError.NoOTPToVerify -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf(
                                    "status" to "Error",
                                    "message" to VerifyEmailError.BadRequest
                                )
                            )
                            println("VerifyEmail: ${response.error.value}")
                            return@post
                        }
                        VerifyEmailError.ServerError ->  {
                            call.respond(
                                HttpStatusCode.ServiceUnavailable,
                                mapOf(
                                    "status" to "Error",
                                    "message" to VerifyEmailError.ServerError.value
                                )
                            )
                            println("VerifyEmail: ${response.error.value}")
                            return@post
                        }
                        VerifyEmailError.UnknownError -> {
                            call.respond(
                                HttpStatusCode.ServiceUnavailable,
                                mapOf(
                                    "status" to "Error",
                                    "message" to VerifyEmailError.UnknownError.value
                                )
                            )
                            return@post
                        }
                    }
                }
            }
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "status" to "Error",
                    "message" to VerifyEmailError.BadRequest.value
                )
            )
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
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "status" to "Error",
                    "message" to "INVALID_REQUEST_FORMAT"
                )
            )
            return@get
        }
        if(!email.matches(emailRegex)) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "status" to "Error",
                    "message" to "INVALID_REQUEST_FORMAT"
                )
            )
            return@get
        }
        val userExists = authenticationRepo.checkUserExistence(email)
        if(userExists == null) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf(
                    "status" to "Error",
                    "message" to "INVALID_REQUEST"
                )
            )
            return@get
        }
        val sent = otpService.sendOTPForVerification(toEmail = email)
        if(sent) {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "Success",
                    "message" to "OTP_SENT"
                )
            )
            return@get
        } else {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                mapOf(
                    "status" to "Error",
                    "message" to "SERVER_ERROR"
                )
            )
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