package com.example.domain.auth

import com.example.data.model.LoginCred
import com.example.data.model.LoginSuccessResponse
import com.example.data.model.SignupCred
import com.example.data.model.User
import com.example.domain.model.network.BaseResponse

interface AuthenticationRepo {
    suspend fun signupUser(signupCred: SignupCred) : BaseResponse<User?>

    suspend fun loginUser(loginCred: LoginCred): BaseResponse<LoginSuccessResponse>

    suspend fun checkUserExistence(email: String): Boolean
}