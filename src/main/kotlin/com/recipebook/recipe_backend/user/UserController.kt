package com.recipebook.recipe_backend.user

import com.recipebook.recipe_backend.security.AuthService
import com.recipebook.recipe_backend.security.CurrentUserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class UserController(
    val userRepository: UserRepository,
    val authService: AuthService,
    val currentUserService: CurrentUserService,
    val httpServletRequest: HttpServletRequest
) {
    @MutationMapping
    fun createUser(
        @Argument username: String,
        @Argument email: String,
        @Argument password: String
    ): String {
        return authService.signUp(username, password, email)
    }

    @MutationMapping
    fun login(
        @Argument username: String,
        @Argument password: String
    ): String {
        return authService.login(username, password)
    }

    @MutationMapping
    fun requestPasswordReset(@Argument email: String): Boolean {
        return authService.requestPasswordReset(email)
    }

    @MutationMapping
    fun resetPassword(
        @Argument token: String,
        @Argument newPassword: String
    ): Boolean {
        return authService.resetPassword(token, newPassword)
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    fun logout(): Boolean {
        val authHeader = httpServletRequest.getHeader("Authorization")
            ?: throw Exception("Missing Authorization header")
        val token = authHeader.removePrefix("Bearer ").trim()
        return authService.logout(token)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun updateUsername(@Argument newUsername: String): User {
        val currentUser = currentUserService.getCurrentUser()

        if (userRepository.findByUsername(newUsername).isPresent) {
            throw Exception("Username already taken: $newUsername")
        }

        currentUser.username = newUsername
        return userRepository.save(currentUser)
    }

    @PreAuthorize("isAuthenticated()")
    @QueryMapping
    fun user(@Argument id: UUID): User? {
        return userRepository.findById(id).orElse(null)
    }

    @PreAuthorize("isAuthenticated()")
    @QueryMapping
    fun users(): List<User> {
        return userRepository.findAll()
    }
}