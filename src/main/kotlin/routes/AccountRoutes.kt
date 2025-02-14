package com.example.routes

import com.example.data.model.User
import com.example.data.model.auth.SignupCred
import com.example.data.model.toDomainUser
import com.example.data.model.user.AvailabilityUpdateDetails
import com.example.data.model.user.NameUpdateDetails
import com.example.domain.account.UserAccountRepository
import com.example.domain.model.network.BaseResponse
import com.example.utils.isHexString
import io.appwrite.Client
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

fun Route.accountRoutes(
    userAccountRepository: UserAccountRepository,
) {
    authenticate {
        post("/user/update-name") {
            val request: NameUpdateDetails = runCatching<NameUpdateDetails?> { call.receiveNullable<NameUpdateDetails>() }
                .getOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Name cannot be empty!")
                return@post
            }
            if(request.name.length < 3) {
                call.respond(HttpStatusCode.BadRequest, "Name must at least contain 3 characters!")
            }
            val userIdString = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized, "Access denied!")
                return@post
            }
            val userId = ObjectId(userIdString)
            val response = userAccountRepository.updateUserName(
                name = request.name,
                userId = userId
            )
            when(response) {
                is BaseResponse.Failure -> {
                    call.respond(HttpStatusCode.ServiceUnavailable, message = response.messageForUser ?: "Some error occurred while updating the name!")
                    return@post
                }
                is BaseResponse.Success -> {
                    call.respond(HttpStatusCode.OK, response.data?.toDomainUser() ?: "Couldn't fetch the updated details!")
                    return@post
                }
            }
        }

        post("/user/update-availability") {
            val request: AvailabilityUpdateDetails =
                runCatching<AvailabilityUpdateDetails?> { call.receiveNullable<AvailabilityUpdateDetails>() }
                    .getOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Enter data in a valid format!")
                    return@post
                }
            if(request.availability.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Availability status cannot be empty!")
            }
            val userIdString = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized, "Access denied!")
                return@post
            }
            val userId = ObjectId(userIdString)
            val response = userAccountRepository.updateUserAvailability(
                availability = request.availability,
                userId = userId
            )
            when (response) {
                is BaseResponse.Failure -> {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        message = response.messageForUser
                            ?: "Some error occurred while updating the availability status!"
                    )
                    return@post
                }

                is BaseResponse.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        response.data?.toDomainUser() ?: "Couldn't fetch the updated details!"
                    )
                    return@post
                }
            }
        }

        post("/user/delete-account") {
            val tempMessage = BaseResponse.Success(
                message = "This service is unavailable now",
                data = null
            )
            call.respond(HttpStatusCode.OK, tempMessage )
            return@post
        }
    }
}