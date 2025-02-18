package com.example.data.model.auth

sealed class ResetPasswordError(val name: String) {
    data object InvalidOTP: ResetPasswordError("INVALID_OTP")
    data object UnknownError: ResetPasswordError("UNKNOWN_ERROR")
    data object InvalidRequestFormat: ResetPasswordError("INVALID_REQUEST_FORMAT")
    data object ServerError: ResetPasswordError("SERVER_ERROR")
}