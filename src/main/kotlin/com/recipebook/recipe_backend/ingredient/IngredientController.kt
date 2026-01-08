package com.recipebook.recipe_backend.ingredient

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class IngredientController(val ingredientRepository: IngredientRepository) {

    @QueryMapping
    fun allIngredients(): List<Ingredient> {
        return ingredientRepository.findAll()
    }

    @MutationMapping
    fun createIngredient(@Argument name: String): Ingredient {
        val newIngredient = Ingredient(name = name)
        return ingredientRepository.save(newIngredient)
    }
}