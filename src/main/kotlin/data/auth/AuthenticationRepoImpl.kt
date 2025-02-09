package com.example.data.auth

import com.example.data.model.LoginCred
import com.example.data.model.LoginSuccessResponse
import com.example.data.model.SignupCred
import com.example.data.model.User
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.model.network.BaseResponse
import com.example.domain.model.security.SaltedHash
import com.example.domain.security.HashingService
import com.example.domain.security.TokenService
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import kotlin.math.log

class AuthenticationRepoImpl(
    private val database: MongoDatabase,
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig
): AuthenticationRepo {
    private val collectionName = "users"

    override suspend fun signupUser(signupCred: SignupCred): BaseResponse<User?> {
        val userExistence = checkUserExistence(signupCred.email)
        if(userExistence) {
            return BaseResponse.Failure(
                errorMessage = "User already exists",
                errorInt = HttpStatusCode.Conflict.value
            )
        } else {
            val saltedHash = hashingService.generateSaltedHash(value = signupCred.password, saltLength = 32)
            val user = User(
                email = signupCred.email,
                fullName = signupCred.name,
                password = saltedHash.hash,
                imageUrl = "",
                id = ObjectId(),
                salt = saltedHash.salt,
            )
            var error : Exception? = null
            val inserted = try {
                database.getCollection<User>(collectionName).insertOne(user).wasAcknowledged()
            } catch (e: Exception) {
                error = e
                false
            }

            return if(inserted) {
                BaseResponse.Success<User?>("User created successfully", data = null)
            } else {
                BaseResponse.Failure(errorMessage = "Error creating user. $error")
            }
        }
    }

    override suspend fun loginUser(loginCred: LoginCred): BaseResponse<LoginSuccessResponse> {
        val userExists = checkUserExistence(loginCred.email)
        return if(!userExists) {
            BaseResponse.Failure(errorMessage = "User does not exists", errorInt = 404)
        } else {
            val user = database.getCollection<User>(collectionName).find(Filters.eq("email", loginCred.email)).firstOrNull()!!
            val verified = hashingService.verify(value = loginCred.password, saltedHash = SaltedHash(salt = user.salt, hash = user.password))

            if(!verified) {
                BaseResponse.Failure(errorInt = 401, errorMessage = "Incorrect credentials")
            } else {
                val token = tokenService.generateToken(
                    tokenConfig = tokenConfig,
                    TokenClaim(
                        name = "userId",
                        value = user.id!!.toHexString()
                    )
                )
                BaseResponse.Success("",LoginSuccessResponse(token))
            }
        }
    }

    override suspend fun checkUserExistence(email: String): Boolean {
        val user: User? = database.getCollection<User>(collectionName)
            .find<User>(Filters.eq("email", email))
            .firstOrNull()
        return user != null
    }
}