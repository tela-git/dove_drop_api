package com.example.domain.email

interface EmailRepository {
    fun sendEmail(toEmail: String, subject: String, body: String): Boolean
}