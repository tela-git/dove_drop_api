package com.example.data.auth

import com.example.data.model.auth.OTP
import com.example.domain.email.EmailRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OTPService(
    private val emailRepository: EmailRepository,
    private val database: MongoDatabase
) {
    private val otpCollection = database.getCollection<OTP>("otps")

    suspend fun sendOTPForVerification(
        toEmail: String
    ) : Boolean {
        val otpRandom = (100000..999999).random().toString()
        val addedToDb = try {
            withContext(Dispatchers.IO) {
                otpCollection.updateOne(
                    Filters.eq("email", toEmail),
                    Updates.combine(
                        Updates.set("expiresAt", System.currentTimeMillis() + 1000L * 60L * 5L),
                        Updates.set("otp", otpRandom),
                    ),
                    UpdateOptions().upsert(true)
                ).wasAcknowledged()
            }
        } catch (e:Exception) {
            println("Error adding otp to the database")
            false
        }

        val sent = try {
            withContext(Dispatchers.IO) {
                emailRepository.sendEmail(
                    toEmail = toEmail,
                    subject = "OTP Verification",
                    body = "Your One Time Password for verification is $otpRandom. This OTP expires in 5 minutes."
                )
            }
        } catch (e: Exception) {
            println("Error sending email to : $toEmail")
            false
        }
        return addedToDb && sent
    }
}