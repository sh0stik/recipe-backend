package com.recipebook.recipe_backend.security

import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService(
    private val userRepository: UserRepository
) {

    fun getCurrentUser(): User {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: throw Exception("Security Context is missing or unauthenticated")

        return userRepository.findByUsername(username)
            .orElseThrow { Exception("Security Context User not found in database") }
    }
}