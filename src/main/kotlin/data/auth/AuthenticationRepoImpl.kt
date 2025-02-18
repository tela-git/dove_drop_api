package com.example.data.auth

import com.example.data.model.*
import com.example.data.model.auth.*
import com.example.domain.auth.AuthenticationRepo
import com.example.domain.model.network.BaseResponse
import com.example.domain.model.network.BaseResult
import com.example.domain.model.security.SaltedHash
import com.example.domain.security.HashingService
import com.example.domain.security.TokenService
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId

class AuthenticationRepoImpl(
    private val database: MongoDatabase,
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
    private val otpService: OTPService
): AuthenticationRepo {
    private val usersCollection = database.getCollection<User>("users")
    private val otpCollection = database.getCollection<OTP>("otps")

    override suspend fun signupUser(signupCred: SignupCred): BaseResponse<User?> {
        val userInDb = checkUserExistence(signupCred.email)
        if(userInDb != null && userInDb.verified) {
            return BaseResponse.Failure(
                errorMessage = SignUpError.UserAlreadyExists.value
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
                verified = false,
                availability = "Available"
            )
            var error : Exception? = null
            val inserted = try {
                if(userInDb != null) {
                    usersCollection.updateOne(
                        Filters.eq("email", signupCred.email),
                        Updates.combine(
                            Updates.set("email", signupCred.email),
                            Updates.set("fullName", signupCred.name),
                            Updates.set("password", saltedHash.hash),
                            Updates.set("imageUrl", ""),
                            Updates.set("salt", saltedHash.salt),
                            Updates.set("verified", false),
                        ),
                        UpdateOptions().upsert(true)
                    ).wasAcknowledged()
                } else {
                    usersCollection.insertOne(user).wasAcknowledged()
                }
            } catch (e: Exception) {
                error = e
                false
            }

            return if(inserted) {
                val otpGenerated = otpService.sendOTPForVerification(signupCred.email)
                if(otpGenerated)
                    BaseResponse.Success<User?>("OTP sent", data = null)
                else BaseResponse.Failure(errorMessage = SignUpError.ErrorSendingOTP.value)
            } else {
                BaseResponse.Failure(errorMessage = SignUpError.UnknownError.value)
            }
        }
    }

    override suspend fun loginUser(loginCred: LoginCred): BaseResponse<LoginSuccessResponse> {
        val userInDb = checkUserExistence(loginCred.email)
        return if(userInDb == null) {
            BaseResponse.Failure(errorMessage = "User does not exists", errorInt = 404)
        } else if(!userInDb.verified) {
            BaseResponse.Failure(errorMessage = "Please verify your email first.", errorInt = 403)
        }
        else {
            val user = usersCollection.find(Filters.eq("email", loginCred.email)).firstOrNull()!!
            val verified = hashingService.verify(value = loginCred.password, saltedHash = SaltedHash(salt = user.salt, hash = user.password))

            if(!verified) {
                BaseResponse.Failure(errorInt = 401, errorMessage = "Incorrect credentials")
            } else {
                val token = tokenService.generateToken(
                    tokenConfig = tokenConfig,
                    TokenClaim(
                        name = "userId",
                        value = user.id!!.toHexString()
                    ),
                    TokenClaim(
                            name = "userEmail",
                    value = user.email
                    )
                )
                BaseResponse.Success("", LoginSuccessResponse(token))
            }
        }
    }

    override suspend fun checkUserExistence(email: String): User? {
        return try {
            usersCollection.find(
                Filters.eq("email", email)
            ).firstOrNull()
        } catch (e: Exception) {
            println("CheckUserExistence: ${e.message}")
            null
        }
    }

    override suspend fun verifyEmail(otpReceivable: OTPReceivable): BaseResult<String, VerifyEmailError> {
        return try {
            val otp = otpCollection.find(
                Filters.eq("email", otpReceivable.email)
            ).firstOrNull()

            if(otp == null) {
                BaseResult.Error(VerifyEmailError.NoOTPToVerify)
            } else {
                if(otp.otp == otpReceivable.otp) {
                    BaseResult.Success("Success")
                } else {
                    BaseResult.Error(VerifyEmailError.InvalidOTP)
                }
            }
        } catch (e: Exception) {
            println("VerifyEmail: ${e.message}")
            BaseResult.Error(VerifyEmailError.UnknownError)
        }
    }

    override suspend fun updateUserToVerified(email: String): Boolean {
        return try {
            return usersCollection.updateOne(
                Filters.eq("email", email),
                Updates.set("verified", true),
                UpdateOptions().upsert(true)
            ).wasAcknowledged()
        } catch (e: Exception) {
            println("UpdateUserToVerified: ${e.message}")
            false
        }
    }

    override suspend fun resetPassword(resetPasswordData: ResetPasswordData): BaseResponse<Boolean> {
        val otp = otpCollection.find(
            Filters.eq("email", resetPasswordData.email)
        ).firstOrNull()

        return if(otp == null || otp.otp != resetPasswordData.otp) {
            BaseResponse.Failure(errorMessage = "Incorrect otp!", errorInt = 400)
        } else {
            val saltedHash = hashingService.generateSaltedHash(value = resetPasswordData.newPassword, saltLength = 32)
            val updated = usersCollection.updateOne(
                Filters.eq("email", resetPasswordData.email),
                Updates.combine(
                    Updates.set("password", saltedHash.hash),
                    Updates.set("salt", saltedHash.salt)
                ),
                UpdateOptions().upsert(true)
            ).wasAcknowledged()
            if(updated) BaseResponse.Success(message = "Password reset successful.", true) else BaseResponse.Failure(errorMessage = "Something went wrong", errorInt = 400)
        }
    }
}