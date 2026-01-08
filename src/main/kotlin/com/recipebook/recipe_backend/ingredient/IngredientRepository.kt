package com.recipebook.recipe_backend.ingredient

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IngredientRepository : JpaRepository<Ingredient, UUID>