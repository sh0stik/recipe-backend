package com.recipebook.recipe_backend.note

import com.recipebook.recipe_backend.recipe.RecipeRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class RecipeNoteController (
    val recipeNoteRepository: RecipeNoteRepository,
    val recipeRepository: RecipeRepository,
    val userRepository: UserRepository,
    val currentUserService: CurrentUserService
) {

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun addNote(
        @Argument recipeId: UUID,
        @Argument note: String
    ): RecipeNote {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }

        // We get the user from the token, NOT from the GraphQL arguments
        val user = currentUserService.getCurrentUser()

        val recipeNote = RecipeNote(note = note, recipe = recipe, user = user)
        return recipeNoteRepository.save(recipeNote)
    }
}