package com.example.data.account

import com.example.data.model.User
import com.example.domain.account.UserAccountRepository
import com.example.domain.model.network.BaseResponse
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import java.util.logging.Filter

class UserAccountRepoImpl(
    private val database: MongoDatabase
): UserAccountRepository {
    private val usersCollection = database.getCollection<User>("users")
    override suspend fun updateUserName(name: String, userId: ObjectId): BaseResponse<User?> {
        return try {
            val updated = usersCollection.updateOne(
                Filters.eq("_id", userId),
                Updates.set("fullName", name),
                UpdateOptions().upsert(false)
            ).wasAcknowledged()
            val user = if(updated) usersCollection.find<User>(Filters.eq("_id", userId)).firstOrNull() else null

            if(updated) {
                BaseResponse.Success("Name updated successfully", data = user)
            } else {
                BaseResponse.Failure("error updating", 503, "Some error occurred while updating the name!")
            }
        } catch (e: Exception) {
            BaseResponse.Failure(e.message.toString(), 503, "Error")
        }

    }

    override suspend fun updateUserAvailability(availability: String, userId: ObjectId): BaseResponse<User?> {
        return try {
            val updated = usersCollection.updateOne(
                Filters.eq("_id", userId),
                Updates.set("availability", availability),
                UpdateOptions().upsert(false)
            ).wasAcknowledged()
            val user = if(updated) usersCollection.find<User>(Filters.eq("_id", userId)).firstOrNull() else null

            if(updated) {
                BaseResponse.Success("Availability status updated successfully", data = user)
            } else {
                BaseResponse.Failure("error updating", 503, "Some error occurred while updating the availability!")
            }
        } catch (e: Exception) {
            BaseResponse.Failure(e.message.toString(), 503, "Error")
        }
    }

    override suspend fun updateUserProfilePic(imageUrl: String, userId: ObjectId): BaseResponse<User?> {
        return try {
            val updated = usersCollection.updateOne(
                Filters.eq("_id", userId),
                Updates.set("imageUrl", imageUrl),
                UpdateOptions().upsert(false)
            ).wasAcknowledged()
            val user = if(updated) usersCollection.find<User>(Filters.eq("_id", userId)).firstOrNull() else null

            if(updated) {
                BaseResponse.Success("Profile picture updated successfully", data = user)
            } else {
                BaseResponse.Failure("error updating", 503, "Some error occurred while updating the profile picture!")
            }
        } catch (e: Exception) {
            BaseResponse.Failure(e.message.toString(), 503, "Error")
        }
    }

    override suspend fun deleteUserAccount(userId: ObjectId): BaseResponse<Boolean> {
        val user = usersCollection.find<User>(
            Filters.eq("_id", userId)
        ).firstOrNull()
        return if(user == null) {
            BaseResponse.Failure("User does not exist", 404,"Account does not exist!")
        } else {
            val userDeleted = usersCollection.deleteOne(
                Filters.eq("_id", userId)
            ).deletedCount

            if(userDeleted >= 1L) {
                BaseResponse.Success("User deleted", true)
            } else {
                BaseResponse.Success("Failed to delete", false)
            }
        }
    }
}