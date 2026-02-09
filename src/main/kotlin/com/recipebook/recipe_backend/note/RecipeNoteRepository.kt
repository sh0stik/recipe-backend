package com.recipebook.recipe_backend.note

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeNoteRepository : JpaRepository<RecipeNote, UUID> {
    fun findByRecipeId(recipeId: UUID): List<RecipeNote>
}