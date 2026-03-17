package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.ingredient.IngredientRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import com.recipebook.recipe_backend.trash.RecipeTrash
import com.recipebook.recipe_backend.trash.RecipeTrashRepository
import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class RecipeController(
    val recipeRepository: RecipeRepository,
    val ingredientRepository: IngredientRepository,
    val recipeIngredientRepository: RecipeIngredientRepository,
    val userRepository: UserRepository,
    val recipeTrashRepository: RecipeTrashRepository,
    val currentUserService: CurrentUserService
) {

    @PreAuthorize("isAuthenticated()")
    @QueryMapping
    fun recipes(): List<Recipe> {
        val currentUser = currentUserService.getCurrentUser()
        return recipeRepository.findVisibleRecipes(currentUser)
    }

    @PreAuthorize("isAuthenticated()")
    @QueryMapping
    fun recipe(@Argument id: UUID): Recipe? {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return null
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.isPublic || recipe.user.id == currentUser.id || recipe.sharedWith.any { it.id == currentUser.id }) {
            return recipe
        }
        throw Exception("Unauthorized: You do not have access to this recipe")
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun createRecipe(@Argument name: String, @Argument description: String?): Recipe {
        val currentUser = currentUserService.getCurrentUser()
        // Fix 1: Automatically assigns the recipe to the token holder
        val newRecipe = Recipe(name = name, description = description, isPublic = false, user = currentUser)
        return recipeRepository.save(newRecipe)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun updateRecipe(
        @Argument recipeId: UUID,
        @Argument name: String?,
        @Argument description: String?,
        @Argument isPublic: Boolean?
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) {
            throw Exception("Unauthorized: You do not own this recipe")
        }

        // Update fields if new values are provided
        name?.let { recipe.name = it }
        description?.let { recipe.description = it }
        isPublic?.let { recipe.isPublic = it }

        return recipeRepository.save(recipe)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun removeIngredientFromRecipe(
        @Argument recipeId: UUID,
        @Argument ingredientId: UUID
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized")

        // Filter out the specific ingredient link
        recipe.ingredients.removeIf { it.ingredient.id == ingredientId }
        return recipeRepository.save(recipe)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun addIngredientToRecipe(
        @Argument recipeId: UUID,
        @Argument ingredientId: UUID,
        @Argument quantity: Float
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized: You do not own this recipe")

        val ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow { Exception("Ingredient not found") }

        val link = RecipeIngredient(
            quantity = quantity,
            ingredient = ingredient,
            recipe = recipe
        )

        recipe.ingredients.add(link)
        return recipeRepository.save(recipe)
    }

    @PreAuthorize("isAuthenticated()")
    @QueryMapping
    fun searchRecipes(
        @Argument keyword: String?,
        @Argument categoryId: UUID?
    ): List<Recipe> {
        val currentUser = currentUserService.getCurrentUser()
        return recipeRepository.searchRecipes(keyword, categoryId, currentUser)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun moveToTrash(@Argument recipeId: UUID): Recipe {
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized: You do not own this recipe")

        recipe.isTrashed = true
        recipeRepository.save(recipe)

        recipeTrashRepository.save(RecipeTrash(recipeId = recipe.id!!))

        return recipe
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun restoreFromTrash(@Argument recipeId: UUID): Recipe {
        val recipe = recipeRepository.findById(recipeId).orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized: You do not own this recipe")

        recipe.isTrashed = false
        recipeRepository.save(recipe)

        if (recipeTrashRepository.existsById(recipeId)) {
            recipeTrashRepository.deleteById(recipeId)
        }

        return recipe
    }
}