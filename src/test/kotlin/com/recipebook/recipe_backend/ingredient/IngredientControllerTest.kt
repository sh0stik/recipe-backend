package com.recipebook.recipe_backend.ingredient

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class IngredientControllerTest {

    @Mock lateinit var ingredientRepository: IngredientRepository

    @Test
    fun `allIngredients returns all`() {
        val ingredients = listOf(
            Ingredient(name = "Salt"),
            Ingredient(name = "Pepper")
        )
        whenever(ingredientRepository.findAll()).thenReturn(ingredients)

        val controller = IngredientController(ingredientRepository)
        val result = controller.allIngredients()

        assertEquals(2, result.size)
    }

    @Test
    fun `createIngredient saves and returns ingredient`() {
        whenever(ingredientRepository.save(any<Ingredient>())).thenAnswer { it.arguments[0] }

        val controller = IngredientController(ingredientRepository)
        val result = controller.createIngredient("Flour", 364f, 10f, 1f, 76f)

        assertEquals("Flour", result.name)
        assertEquals(364f, result.caloriesPer100g)
        assertEquals(10f, result.proteinPer100g)
        assertEquals(1f, result.fatPer100g)
        assertEquals(76f, result.carbsPer100g)
    }
}
