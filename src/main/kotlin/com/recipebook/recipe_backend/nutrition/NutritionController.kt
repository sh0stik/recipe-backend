package com.recipebook.recipe_backend.nutrition

import com.recipebook.recipe_backend.recipe.Recipe
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class NutritionController(val nutritionService: NutritionService) {

    @SchemaMapping(typeName = "Recipe", field = "nutrition")
    fun nutrition(recipe: Recipe): NutritionInfo? {
        return nutritionService.calculate(recipe)
    }
}
