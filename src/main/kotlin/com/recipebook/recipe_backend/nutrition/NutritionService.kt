package com.recipebook.recipe_backend.nutrition

import com.recipebook.recipe_backend.recipe.Recipe
import org.springframework.stereotype.Service

@Service
class NutritionService {

    fun calculate(recipe: Recipe): NutritionInfo? {
        val ingredients = recipe.ingredients
        if (ingredients.isEmpty()) return null

        val totalWeight = ingredients.sumOf { it.quantity.toDouble() }.toFloat()
        if (totalWeight == 0f) return null

        var totalCalories = 0f
        var totalProtein = 0f
        var totalFat = 0f
        var totalCarbs = 0f

        for (ri in ingredients) {
            val factor = ri.quantity / 100f
            totalCalories += ri.ingredient.caloriesPer100g * factor
            totalProtein += ri.ingredient.proteinPer100g * factor
            totalFat += ri.ingredient.fatPer100g * factor
            totalCarbs += ri.ingredient.carbsPer100g * factor
        }

        val normFactor = 100f / totalWeight

        return NutritionInfo(
            caloriesPer100g = totalCalories * normFactor,
            proteinPer100g = totalProtein * normFactor,
            fatPer100g = totalFat * normFactor,
            carbsPer100g = totalCarbs * normFactor,
            totalWeight = totalWeight
        )
    }
}
