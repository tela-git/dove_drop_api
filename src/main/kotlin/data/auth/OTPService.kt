package com.example.data.auth

import com.example.data.model.OTP
import com.example.domain.email.EmailRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import net.mamoe.yamlkt.toYamlElementOrNull
import org.bson.types.ObjectId
import java.util.Date
import kotlin.random.Random

class OTPService(
    private val emailRepository: EmailRepository,
    private val database: MongoDatabase
) {
    val otpCollection = database.getCollection<OTP>("otps")

    suspend fun sendOTPForVerification(
        toEmail: String
    ) : Boolean {
        val otpRandom = (100000..999999).random().toString()
        val addedToDb = otpCollection.updateOne(
            Filters.eq("email", toEmail),
            Updates.combine(
                Updates.set("email", toEmail),
                Updates.set("expiresAt", System.currentTimeMillis() + 1000L * 60L * 5L),
                Updates.set("otp",otpRandom),
            ),
            UpdateOptions().upsert(true)
        ).wasAcknowledged()

        val sent =  emailRepository.sendEmail(
            toEmail = toEmail,
            subject = "OTP Verification",
            body = "Your One Time Password for verification is $otpRandom. This OTP expires in 5 minutes."
        )
        return addedToDb && sent
    }
}