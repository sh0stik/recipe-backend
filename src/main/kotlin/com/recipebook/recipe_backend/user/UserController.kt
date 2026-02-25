package com.recipebook.recipe_backend.user

import com.recipebook.recipe_backend.security.AuthService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class UserController (val userRepository: UserRepository,
                      val authService: AuthService) {
    @MutationMapping
    fun createUser(@Argument username: String,
                   @Argument email: String,
                   @Argument password: String) : String{
        return authService.signUp(username, password, email)
    }

    @QueryMapping
    fun user(@Argument id: UUID): User? {
        return userRepository.findById(id).orElse(null)
    }

    @QueryMapping
    fun users(): List<User>{
        return userRepository.findAll()
    }
}