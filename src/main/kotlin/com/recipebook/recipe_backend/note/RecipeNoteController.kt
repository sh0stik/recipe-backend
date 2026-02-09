package com.recipebook.recipe_backend.note

import com.recipebook.recipe_backend.recipe.RecipeRepository
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class RecipeNoteController (
    val recipeNoteRepository: RecipeNoteRepository,
    val recipeRepository: RecipeRepository,
    val userRepository: UserRepository
) {

    @MutationMapping
    fun addNote(
        @Argument recipeId: UUID,
        @Argument userId: UUID,
        @Argument note: String
    ): RecipeNote {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }

        return recipeNoteRepository.save(
            RecipeNote(note = note, recipe = recipe, user = user)
        )
    }
}