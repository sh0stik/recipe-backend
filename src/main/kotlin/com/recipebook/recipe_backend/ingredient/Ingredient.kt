package com.recipebook.recipe_backend.ingredient

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "ingredients")
data class Ingredient(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val name: String,

    @Column(name = "calories_per_100g")
    val caloriesPer100g: Float = 0f,

    @Column(name = "protein_per_100g")
    val proteinPer100g: Float = 0f,

    @Column(name = "fat_per_100g")
    val fatPer100g: Float = 0f,

    @Column(name = "carbs_per_100g")
    val carbsPer100g: Float = 0f
)