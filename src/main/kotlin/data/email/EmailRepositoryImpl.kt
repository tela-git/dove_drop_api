package com.example.data.email

import com.example.domain.email.EmailRepository
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailRepositoryImpl(): EmailRepository {
    override fun sendEmail(toEmail: String, subject: String, body: String): Boolean {
        val username = "${System.getenv("email")}@gmail.com"
        val password = System.getenv("emailPassword")

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
            return true
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }
    }
}