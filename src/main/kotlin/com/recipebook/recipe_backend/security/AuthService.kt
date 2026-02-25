package com.recipebook.recipe_backend.security

import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService
) {
    fun signUp(username: String, password: String, email: String): String {
        if (userRepository.findByUsername(username).isPresent){
            throw Exception("Username already taken: $username")
        }

        val user = User(
            username = username,
            password = passwordEncoder.encode(password)!!,
            role = "ROLE_USER",
            email = email
        )
        userRepository.save(user)

        return jwtService.generateToken(CustomUserDetails(user))
    }

    fun login(username: String, password: String): String {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )

        val user = userRepository.findByUsername(username)
            .orElseThrow { Exception("User not found") }

        return jwtService.generateToken(CustomUserDetails(user))
    }

    fun requestPasswordReset(email: String): Boolean{
        val user = userRepository.findUserByEmail(email)
            .orElseThrow { Exception("User not found with email: $email") }

        val tokenString = UUID.randomUUID().toString()

        val expiry = Instant.now().plusSeconds(15 * 60)

        val resetToken = PasswordResetToken(
            token = tokenString,
            expiryDate = expiry,
            user = user
        )

        passwordResetTokenRepository.save(resetToken)

        emailService.sendPasswordResetEmail(user.email, tokenString)

        return true

    }
}