package com.example.domain.account

import com.example.data.model.User
import com.example.domain.model.network.BaseResponse
import org.bson.types.ObjectId

interface UserAccountRepository {
    suspend fun updateUserName(name: String, userId: ObjectId): BaseResponse<User?>
    suspend fun updateUserAvailability(availability: String, userId: ObjectId): BaseResponse<User?>
    suspend fun updateUserProfilePic(imageUrl: String, userId: ObjectId): BaseResponse<User?>
    suspend fun deleteUserAccount(userId: ObjectId): BaseResponse<Boolean>
}