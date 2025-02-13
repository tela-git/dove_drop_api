package com.example.domain.model.network

import kotlinx.serialization.Serializable

sealed class BaseResponse<out T> {
    @Serializable
    data class Success<out T>(val message: String, val data: T): BaseResponse<T>()
    @Serializable
    data class Failure(
        val errorMessage: String,
        val errorInt: Int? = null,
        val messageForUser: String? = null
    ): BaseResponse<Nothing>()
}