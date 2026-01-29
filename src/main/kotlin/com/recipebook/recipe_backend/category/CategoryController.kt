package com.recipebook.recipe_backend.category

import com.recipebook.recipe_backend.recipe.Recipe
import com.recipebook.recipe_backend.recipe.RecipeRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class CategoryController(
    val categoryRepository: CategoryRepository,
    val recipeRepository: RecipeRepository
) {
    @QueryMapping
    fun allCategories(): List<Category>{
        return categoryRepository.findAll()
    }

    @MutationMapping
    fun createCategory(
        @Argument name: String,
        @Argument parentCategoryId: UUID?
    ): Category{
        val parent = parentCategoryId?.let{
            categoryRepository.findById(it).orElse(null)
        }
        return categoryRepository.save(Category(name = name, parentCategory = parent))
    }

    @MutationMapping
    fun addCategoryToRecipe(
        @Argument recipeId: UUID,
        @Argument categoryId: UUID
    ): Recipe {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { Exception("Recipe not found") }
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { Exception("Category not found") }

        recipe.categories.add(category)
        return recipeRepository.save(recipe)
    }
}