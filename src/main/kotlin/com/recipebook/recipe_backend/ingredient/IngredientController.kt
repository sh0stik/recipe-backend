package com.recipebook.recipe_backend.ingredient

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller

@Controller
class IngredientController(val ingredientRepository: IngredientRepository) {

    @QueryMapping
    fun allIngredients(): List<Ingredient> {
        return ingredientRepository.findAll()
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun createIngredient(
        @Argument name: String,
        @Argument caloriesPer100g: Float,
        @Argument proteinPer100g: Float,
        @Argument fatPer100g: Float,
        @Argument carbsPer100g: Float
    ): Ingredient {
        val newIngredient = Ingredient(
            name = name,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g
        )
        return ingredientRepository.save(newIngredient)
    }
}