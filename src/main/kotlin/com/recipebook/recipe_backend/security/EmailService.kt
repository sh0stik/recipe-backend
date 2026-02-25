package com.recipebook.recipe_backend.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val fromEmail: String
) {

    fun sendPasswordResetEmail(toEmail: String, token: String) {
        val message = SimpleMailMessage()
        message.from = fromEmail
        message.setTo(toEmail)
        message.subject = "Recipe App - Password Reset Request"

        // This will be the link to your frontend application
        val resetUrl = "http://localhost:3000/reset-password?token=$token"

        message.text = """
            Hello,
            
            You recently requested to reset your password for your Recipe App account. 
            Click the link below to reset it:
            
            $resetUrl
            
            If you did not request a password reset, please ignore this email.
            This link will expire in 15 minutes.
        """.trimIndent()

        mailSender.send(message)
    }
}