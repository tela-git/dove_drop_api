package com.example.domain.model.network

interface Error

sealed interface BaseResult<out D, out E> {
    data class Success<out D>(val data: D): BaseResult<D, Nothing>
    data class Error<out E>(val error: E): BaseResult<Nothing, E>
}

