package com.recipebook.recipe_backend.user

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class UserController (val userRepository: UserRepository) {
    @MutationMapping
    fun createUser(@Argument username: String): User {
        return userRepository.save(User(username = username))
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