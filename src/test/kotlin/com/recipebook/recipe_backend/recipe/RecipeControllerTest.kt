package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.ingredient.Ingredient
import com.recipebook.recipe_backend.ingredient.IngredientRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import com.recipebook.recipe_backend.trash.RecipeTrashRepository
import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class RecipeControllerTest {

    @Mock lateinit var recipeRepository: RecipeRepository
    @Mock lateinit var ingredientRepository: IngredientRepository
    @Mock lateinit var recipeIngredientRepository: RecipeIngredientRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var recipeTrashRepository: RecipeTrashRepository
    @Mock lateinit var currentUserService: CurrentUserService

    private lateinit var controller: RecipeController

    private val userId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()
    private val owner = User(id = userId, username = "owner", email = "owner@test.com", password = "pass")
    private val otherUser = User(id = otherUserId, username = "other", email = "other@test.com", password = "pass")
    private val recipeId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        controller = RecipeController(
            recipeRepository, ingredientRepository, recipeIngredientRepository,
            userRepository, recipeTrashRepository, currentUserService
        )
    }

    private fun makeRecipe(user: User = owner, public: Boolean = false): Recipe {
        return Recipe(id = recipeId, name = "Test Recipe", user = user, isPublic = public)
    }

    @Test
    fun `createRecipe assigns recipe to current user`() {
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.createRecipe("My Recipe", "Description")

        assertEquals("My Recipe", result.name)
        assertEquals(owner, result.user)
        assertFalse(result.isPublic)
    }

    @Test
    fun `recipes returns visible recipes for current user`() {
        val recipes = listOf(makeRecipe())
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.findVisibleRecipes(owner)).thenReturn(recipes)

        val result = controller.recipes()

        assertEquals(1, result.size)
        assertEquals("Test Recipe", result[0].name)
    }

    @Test
    fun `recipe returns public recipe`() {
        val recipe = makeRecipe(public = true)
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        val result = controller.recipe(recipeId)

        assertNotNull(result)
        assertEquals("Test Recipe", result!!.name)
    }

    @Test
    fun `recipe returns own private recipe`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)

        val result = controller.recipe(recipeId)

        assertNotNull(result)
    }

    @Test
    fun `recipe throws for unauthorized access to private recipe`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.recipe(recipeId)
        }
    }

    @Test
    fun `recipe returns null for nonexistent id`() {
        whenever(recipeRepository.findById(any<UUID>())).thenReturn(Optional.empty())

        assertNull(controller.recipe(UUID.randomUUID()))
    }

    @Test
    fun `updateRecipe updates fields for owner`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.updateRecipe(recipeId, "Updated", "New desc", true)

        assertEquals("Updated", result.name)
        assertEquals("New desc", result.description)
        assertTrue(result.isPublic)
    }

    @Test
    fun `updateRecipe throws for non-owner`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.updateRecipe(recipeId, "Hack", null, null)
        }
    }

    @Test
    fun `addIngredientToRecipe adds ingredient for owner`() {
        val recipe = makeRecipe()
        val ingredientId = UUID.randomUUID()
        val ingredient = Ingredient(id = ingredientId, name = "Salt", caloriesPer100g = 0f)

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient))
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.addIngredientToRecipe(recipeId, ingredientId, 50f)

        assertEquals(1, result.ingredients.size)
        assertEquals(50f, result.ingredients[0].quantity)
    }

    @Test
    fun `addIngredientToRecipe throws for non-owner`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.addIngredientToRecipe(recipeId, UUID.randomUUID(), 100f)
        }
    }

    @Test
    fun `removeIngredientFromRecipe removes correct ingredient`() {
        val ingredientId = UUID.randomUUID()
        val ingredient = Ingredient(id = ingredientId, name = "Salt")
        val recipe = makeRecipe()
        recipe.ingredients.add(RecipeIngredient(id = UUID.randomUUID(), quantity = 10f, ingredient = ingredient, recipe = recipe))

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }

        val result = controller.removeIngredientFromRecipe(recipeId, ingredientId)

        assertTrue(result.ingredients.isEmpty())
    }

    @Test
    fun `moveToTrash sets isTrashed and saves trash record`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }
        whenever(recipeTrashRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = controller.moveToTrash(recipeId)

        assertTrue(result.isTrashed)
        verify(recipeTrashRepository).save(any())
    }

    @Test
    fun `moveToTrash throws for non-owner`() {
        val recipe = makeRecipe()
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(otherUser)

        assertThrows(Exception::class.java) {
            controller.moveToTrash(recipeId)
        }
    }

    @Test
    fun `restoreFromTrash untrashes recipe`() {
        val recipe = makeRecipe()
        recipe.isTrashed = true
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.save(any<Recipe>())).thenAnswer { it.arguments[0] }
        whenever(recipeTrashRepository.existsById(recipeId)).thenReturn(true)

        val result = controller.restoreFromTrash(recipeId)

        assertFalse(result.isTrashed)
        verify(recipeTrashRepository).deleteById(recipeId)
    }

    @Test
    fun `searchRecipes delegates to repository with current user`() {
        whenever(currentUserService.getCurrentUser()).thenReturn(owner)
        whenever(recipeRepository.searchRecipes("pasta", null, owner)).thenReturn(listOf(makeRecipe()))

        val result = controller.searchRecipes("pasta", null)

        assertEquals(1, result.size)
    }
}
