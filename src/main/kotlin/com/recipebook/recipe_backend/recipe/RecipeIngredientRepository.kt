package com.recipebook.recipe_backend.recipe

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, UUID>