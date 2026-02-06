package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.ingredient.IngredientRepository
import com.recipebook.recipe_backend.trash.RecipeTrash
import com.recipebook.recipe_backend.trash.RecipeTrashRepository
import com.recipebook.recipe_backend.user.UserRepository
import org.hibernate.engine.jdbc.env.spi.AnsiSqlKeywords
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class RecipeController(
    val recipeRepository: RecipeRepository,
    val ingredientRepository: IngredientRepository,
    val recipeIngredientRepository: RecipeIngredientRepository,
    val userRepository: UserRepository,
    val recipeTrashRepository: RecipeTrashRepository
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
    fun createRecipe(@Argument name: String, @Argument description: String?, @Argument userId: UUID): Recipe {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        val newRecipe = Recipe(name = name, description = description, isPublic = false, user = user)
        return recipeRepository.save(newRecipe)
    }

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

        val link = RecipeIngredient(
            amount = amount,
            ingredient = ingredient,
            recipe = recipe
        )

        recipe.ingredients.add(link)
        return recipeRepository.save(recipe)
    }

    @QueryMapping
    fun searchRecipes(
        @Argument keyword: String?,
        @Argument categoryId: UUID?
    ): List<Recipe> {
        return recipeRepository.searchRecipes(keyword, categoryId)
    }

    @MutationMapping
    fun moveToTrash(@Argument recipeId: UUID): Recipe{
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("Recipe not found") }

        recipe.isTrashed = true
        recipeRepository.save(recipe)

        recipeTrashRepository.save(RecipeTrash(recipeId = recipe.id!!))

        return recipe
    }

    @MutationMapping
    fun restoreFromTrash(@Argument recipeId: UUID): Recipe{
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("recipe not found") }

        recipe.isTrashed = false
        recipeRepository.save(recipe)

        if (recipeTrashRepository.existsById(recipeId)){
            recipeTrashRepository.deleteById(recipeId)
        }

        return recipe
    }
}