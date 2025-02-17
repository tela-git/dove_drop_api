package com.example.data.model.auth

sealed class SignUpError(val value: String) {
    data object UserAlreadyExists : SignUpError("USER_ALREADY_EXISTS")
    data object ErrorSendingOTP: SignUpError("ERROR_SENDING_OTP")
    data object UnknownError: SignUpError("UNKNOWN_ERROR")
    data object InvalidCredFormat: SignUpError("INVALID_CRED_FORMAT")
}