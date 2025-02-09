package com.example.routes

import com.example.data.model.LoginCred
import com.example.data.model.LoginSuccessResponse
import com.example.data.model.SignupCred
import com.example.data.model.User
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.model.network.BaseResponse
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger

fun Route.signUp(
    authenticationRepo: AuthenticationRepo
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
                call.respond(HttpStatusCode.Created, "User created successfully.")
                return@post
            }
        }
    }

    post("/auth/login") {
        val loginCred: LoginCred = runCatching<LoginCred?> { call.receiveNullable<LoginCred>() }.getOrNull() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
            return@post
        }
        val response = authenticationRepo.loginUser(loginCred)
        when(response) {
            is BaseResponse.Failure -> {
                val logging = response.errorMessage
                when(response.errorInt) {
                    404 ->  call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
                    401 ->  call.respond(HttpStatusCode.BadRequest, "Enter valid details!")
                    else ->  call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
                }
            }
            is BaseResponse.Success<LoginSuccessResponse> -> {
                call.respond(HttpStatusCode.OK, response.data.token)
            }
        }
    }
}