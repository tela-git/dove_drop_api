package com.example.domain.auth

import com.example.data.model.*
import com.example.data.model.auth.*
import com.example.domain.model.network.BaseResponse
import com.example.domain.model.network.BaseResult

interface AuthenticationRepo {
    suspend fun signupUser(signupCred: SignupCred) : BaseResponse<User?>

    suspend fun loginUser(loginCred: LoginCred): BaseResponse<LoginSuccessResponse>

    suspend fun checkUserExistence(email: String): User?

    suspend fun verifyEmail(otpReceivable: OTPReceivable): BaseResult<String, VerifyEmailError>

    suspend fun updateUserToVerified(email:String): Boolean

    suspend fun resetPassword(resetPasswordData: ResetPasswordData): BaseResult<String, ResetPasswordError>
}