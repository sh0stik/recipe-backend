package com.recipebook.recipe_backend.security

import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val blacklistedTokenRepository: BlacklistedTokenRepository
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

    @Transactional // Why: Ensures all DB changes succeed or fail together
    fun requestPasswordReset(email: String): Boolean {
        val user = userRepository.findUserByEmail(email).orElse(null) ?: return true

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

    @Transactional
    fun resetPassword(token: String, newPassword: String): Boolean {
        val resetToken = passwordResetTokenRepository.findByToken(token).orElse(null)
            ?: return false

        if (resetToken.expiryDate.isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken)
            return false
        }

        val user = resetToken.user
        user.password = passwordEncoder.encode(newPassword)!!
        userRepository.save(user)

        passwordResetTokenRepository.delete(resetToken)

        return true
    }

    @Transactional
    fun logout(token: String): Boolean {
        if (blacklistedTokenRepository.existsByToken(token)) return true

        // 1. Ask JwtService to decode the token and find its expiration date
        val expirationDate = jwtService.extractExpiration(token)

        // 2. Save both the string and the date to the database
        val blacklistedToken = BlacklistedToken(
            token = token,
            expiryDate = expirationDate.toInstant() // Convert java.util.Date to Instant
        )
        blacklistedTokenRepository.save(blacklistedToken)

        return true
    }
}