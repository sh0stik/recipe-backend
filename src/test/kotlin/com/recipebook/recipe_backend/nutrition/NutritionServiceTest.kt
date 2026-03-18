package com.recipebook.recipe_backend.nutrition

import com.recipebook.recipe_backend.ingredient.Ingredient
import com.recipebook.recipe_backend.recipe.Recipe
import com.recipebook.recipe_backend.recipe.RecipeIngredient
import com.recipebook.recipe_backend.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NutritionServiceTest {

    private lateinit var service: NutritionService
    private lateinit var owner: User

    @BeforeEach
    fun setUp() {
        service = NutritionService()
        owner = User(username = "testuser", email = "test@test.com", password = "pass")
    }

    @Test
    fun `returns null for recipe with no ingredients`() {
        val recipe = Recipe(name = "Empty", user = owner)
        assertNull(service.calculate(recipe))
    }

    @Test
    fun `single ingredient returns that ingredient's per-100g values`() {
        val spaghetti = Ingredient(
            name = "Spaghetti",
            caloriesPer100g = 158f,
            proteinPer100g = 5.8f,
            fatPer100g = 0.9f,
            carbsPer100g = 31f
        )
        val recipe = Recipe(name = "Pasta", user = owner)
        recipe.ingredients.add(RecipeIngredient(quantity = 200f, ingredient = spaghetti, recipe = recipe))

        val result = service.calculate(recipe)!!

        assertEquals(200f, result.totalWeight)
        assertEquals(158f, result.caloriesPer100g, 0.01f)
        assertEquals(5.8f, result.proteinPer100g, 0.01f)
        assertEquals(0.9f, result.fatPer100g, 0.01f)
        assertEquals(31f, result.carbsPer100g, 0.01f)
    }

    @Test
    fun `two ingredients blends nutrition proportionally`() {
        val spaghetti = Ingredient(
            name = "Spaghetti",
            caloriesPer100g = 158f,
            proteinPer100g = 5.8f,
            fatPer100g = 0.9f,
            carbsPer100g = 31f
        )
        val egg = Ingredient(
            name = "Egg",
            caloriesPer100g = 155f,
            proteinPer100g = 13f,
            fatPer100g = 11f,
            carbsPer100g = 1.1f
        )

        val recipe = Recipe(name = "Carbonara", user = owner)
        recipe.ingredients.add(RecipeIngredient(quantity = 200f, ingredient = spaghetti, recipe = recipe))
        recipe.ingredients.add(RecipeIngredient(quantity = 120f, ingredient = egg, recipe = recipe))

        val result = service.calculate(recipe)!!

        assertEquals(320f, result.totalWeight)

        // Manual: total cal = 158*2 + 155*1.2 = 316 + 186 = 502
        // Per 100g = 502 / 320 * 100 = 156.875
        assertEquals(156.875f, result.caloriesPer100g, 0.01f)

        // total protein = 5.8*2 + 13*1.2 = 11.6 + 15.6 = 27.2
        // Per 100g = 27.2 / 320 * 100 = 8.5
        assertEquals(8.5f, result.proteinPer100g, 0.01f)

        // total fat = 0.9*2 + 11*1.2 = 1.8 + 13.2 = 15.0
        // Per 100g = 15.0 / 320 * 100 = 4.6875
        assertEquals(4.6875f, result.fatPer100g, 0.01f)

        // total carbs = 31*2 + 1.1*1.2 = 62 + 1.32 = 63.32
        // Per 100g = 63.32 / 320 * 100 = 19.7875
        assertEquals(19.7875f, result.carbsPer100g, 0.01f)
    }

    @Test
    fun `ingredient with zero nutritional values contributes weight but no nutrients`() {
        val water = Ingredient(
            name = "Water",
            caloriesPer100g = 0f,
            proteinPer100g = 0f,
            fatPer100g = 0f,
            carbsPer100g = 0f
        )
        val sugar = Ingredient(
            name = "Sugar",
            caloriesPer100g = 400f,
            proteinPer100g = 0f,
            fatPer100g = 0f,
            carbsPer100g = 100f
        )

        val recipe = Recipe(name = "Sugar Water", user = owner)
        recipe.ingredients.add(RecipeIngredient(quantity = 900f, ingredient = water, recipe = recipe))
        recipe.ingredients.add(RecipeIngredient(quantity = 100f, ingredient = sugar, recipe = recipe))

        val result = service.calculate(recipe)!!

        assertEquals(1000f, result.totalWeight)
        // 400 total cal / 1000g * 100 = 40 cal per 100g
        assertEquals(40f, result.caloriesPer100g, 0.01f)
        assertEquals(0f, result.proteinPer100g, 0.01f)
        assertEquals(0f, result.fatPer100g, 0.01f)
        // 100 total carbs / 1000g * 100 = 10g per 100g
        assertEquals(10f, result.carbsPer100g, 0.01f)
    }

    @Test
    fun `small quantities calculate correctly`() {
        val salt = Ingredient(
            name = "Salt",
            caloriesPer100g = 0f,
            proteinPer100g = 0f,
            fatPer100g = 0f,
            carbsPer100g = 0f
        )

        val recipe = Recipe(name = "Pinch of salt", user = owner)
        recipe.ingredients.add(RecipeIngredient(quantity = 5f, ingredient = salt, recipe = recipe))

        val result = service.calculate(recipe)!!

        assertEquals(5f, result.totalWeight)
        assertEquals(0f, result.caloriesPer100g, 0.01f)
    }

    @Test
    fun `recalculates after ingredient removal`() {
        val flour = Ingredient(name = "Flour", caloriesPer100g = 364f, proteinPer100g = 10f, fatPer100g = 1f, carbsPer100g = 76f)
        val butter = Ingredient(name = "Butter", caloriesPer100g = 717f, proteinPer100g = 0.9f, fatPer100g = 81f, carbsPer100g = 0.1f)

        val recipe = Recipe(name = "Dough", user = owner)
        val flourLink = RecipeIngredient(quantity = 500f, ingredient = flour, recipe = recipe)
        val butterLink = RecipeIngredient(quantity = 200f, ingredient = butter, recipe = recipe)
        recipe.ingredients.add(flourLink)
        recipe.ingredients.add(butterLink)

        val before = service.calculate(recipe)!!
        assertEquals(700f, before.totalWeight)

        // Remove butter
        recipe.ingredients.remove(butterLink)

        val after = service.calculate(recipe)!!
        assertEquals(500f, after.totalWeight)
        // With only flour, should match flour's per-100g values
        assertEquals(364f, after.caloriesPer100g, 0.01f)
        assertEquals(10f, after.proteinPer100g, 0.01f)
    }
}
