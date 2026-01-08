package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.ingredient.IngredientRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class RecipeController(
    val recipeRepository: RecipeRepository,
    val ingredientRepository: IngredientRepository, // We need this to find "Flour"
    val recipeIngredientRepository: RecipeIngredientRepository // We need this to save the link
) {

    @QueryMapping
    fun recipes(): List<Recipe> {
        return recipeRepository.findAll()
    }

    @QueryMapping
    fun recipe(@Argument id: UUID): Recipe? {
        return recipeRepository.findById(id).orElse(null)
    }

    @MutationMapping
    fun createRecipe(@Argument name: String, @Argument description: String?): Recipe {
        val newRecipe = Recipe(name = name, description = description, isPublic = false)
        return recipeRepository.save(newRecipe)
    }

    // --- NEW METHOD: Link Existing Ingredient to Recipe ---
    @MutationMapping
    fun addIngredientToRecipe(
        @Argument recipeId: UUID,
        @Argument ingredientId: UUID,
        @Argument amount: String
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }

        val ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow { Exception("Ingredient not found") }

        // Create the link (RecipeIngredient)
        val link = RecipeIngredient(
            amount = amount,
            ingredient = ingredient,
            recipe = recipe
        )

        // Add to recipe's list and save
        recipe.ingredients.add(link)
        return recipeRepository.save(recipe)
    }
}