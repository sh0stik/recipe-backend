package com.recipebook.recipe_backend.category

import com.recipebook.recipe_backend.recipe.Recipe
import com.recipebook.recipe_backend.recipe.RecipeRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class CategoryController(
    val categoryRepository: CategoryRepository,
    val recipeRepository: RecipeRepository,
    val currentUserService: CurrentUserService
) {
    @QueryMapping
    fun allCategories(): List<Category> {
        return categoryRepository.findAll()
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun createCategory(
        @Argument name: String,
        @Argument parentCategoryId: UUID?
    ): Category {
        val parent = parentCategoryId?.let {
            categoryRepository.findById(it).orElseThrow { Exception("Parent category not found") }
        }
        return categoryRepository.save(Category(name = name, parentCategory = parent))
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun addCategoryToRecipe(
        @Argument recipeId: UUID,
        @Argument categoryId: UUID
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized: You do not own this recipe")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow { Exception("Category not found") }

        recipe.categories.add(category)
        return recipeRepository.save(recipe)
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    fun removeCategoryFromRecipe(
        @Argument recipeId: UUID,
        @Argument categoryId: UUID
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }
        val currentUser = currentUserService.getCurrentUser()

        if (recipe.user.id != currentUser.id) throw Exception("Unauthorized: You do not own this recipe")

        recipe.categories.removeIf { it.id == categoryId }
        return recipeRepository.save(recipe)
    }
}