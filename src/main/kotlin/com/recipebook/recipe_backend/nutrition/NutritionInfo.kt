package com.recipebook.recipe_backend.nutrition

data class NutritionInfo(
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val totalWeight: Float
)
