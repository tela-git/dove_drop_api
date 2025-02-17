package com.example.data.model.auth

sealed class VerifyEmailError(val value: String) {
    data object InvalidOTP: VerifyEmailError("INVALID_OTP")
    data object ServerError: VerifyEmailError("SERVER_ERROR")
    data object BadRequest: VerifyEmailError("INVALID_DATA_FORMAT")
    data object UnknownError: VerifyEmailError("UNKNOWN_ERROR")
    data object NoOTPToVerify: VerifyEmailError("NO_OTP_TO_VERIFY")
}