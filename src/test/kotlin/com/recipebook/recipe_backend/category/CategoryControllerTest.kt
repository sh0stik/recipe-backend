package com.recipebook.recipe_backend.category

import com.recipebook.recipe_backend.recipe.Recipe
import com.recipebook.recipe_backend.recipe.RecipeRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import com.recipebook.recipe_backend.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class CategoryControllerTest {

    @Mock lateinit var categoryRepository: CategoryRepository
    @Mock lateinit var recipeRepository: RecipeRepository
    @Mock lateinit var currentUserService: CurrentUserService

    private lateinit var controller: CategoryController

    private val userId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()
    private val owner = User(id = userId, username = "owner", email = "o@test.com", password = "pass")
    private val otherUser = User(id = otherUserId, username = "other", email = "ot@test.com", password = "pass")

    @BeforeEach
    fun setUp() {
        controller = CategoryController(categoryRepository, recipeRepository, currentUserService)
    }

    @Test
    fun `allCategories returns all`() {
        val cats = listOf(Category(name = "Italian"), Category(name = "Desserts"))
        whenever(categoryRepository.findAll()).thenReturn(cats)

        assertEquals(2, controller.allCategories().size)
    }

    @Test
    fun `createCategory without parent`() {
        whenever(categoryRepository.save(any<Category>())).thenAnswer { it.arguments[0] }

        val result = controller.createCategory("Italian", null)

        assertEquals("Italian", result.name)
        assertNull(result.parentCategory)
    }

    @Test
    fun `createCategory with parent`() {
        val parentId = UUID.randomUUID()
        val parent = Category(id = parentId, name = "Desserts")
        whenever(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent))
        whenever(categoryRepository.save(any<Category>())).thenAnswer { it.arguments[0] }

        val result = controller.createCategory("Cakes", parentId)

        assertEquals("Cakes", result.name)
        assertEquals(parent, result.parentCategory)
    }

    @Test
    fun `createCategory throws when parent not found`() {
        val fakeId = UUID.randomUUID()
        whenever(categoryRepository.findById(fakeId)).thenReturn(Optional.empty())

        assertThrows(Exception::class.java) {
            controller.createCategory("Orphan", fakeId)
        }
    }

    @Test
    fun `addCategoryToRecipe succeeds for owner`() {
        val recipeId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()
        val recipe = Recipe(id = recipeId, name = "Pasta", user = owner)
        val category = Category(id = categoryId, name = "Italian")

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.addCategoryToRecipe(recipeId, categoryId)

        assertTrue(result.categories.any { it.name == "Italian" })
    }

    @Test
    fun `addCategoryToRecipe throws for non-owner`() {
        val recipeId = UUID.randomUUID()
        val recipe = Recipe(id = recipeId, name = "Pasta", user = owner)

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.addCategoryToRecipe(recipeId, UUID.randomUUID())
        }
    }

    @Test
    fun `removeCategoryFromRecipe removes category for owner`() {
        val recipeId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()
        val category = Category(id = categoryId, name = "Italian")
        val recipe = Recipe(id = recipeId, name = "Pasta", user = owner)
        recipe.categories.add(category)

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.removeCategoryFromRecipe(recipeId, categoryId)

        assertTrue(result.categories.isEmpty())
    }

    @Test
    fun `removeCategoryFromRecipe throws for non-owner`() {
        val recipeId = UUID.randomUUID()
        val recipe = Recipe(id = recipeId, name = "Pasta", user = owner)

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.removeCategoryFromRecipe(recipeId, UUID.randomUUID())
        }
    }
}
