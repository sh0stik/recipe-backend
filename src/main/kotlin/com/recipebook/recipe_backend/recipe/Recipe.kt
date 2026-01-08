package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.ingredient.Ingredient
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "recipes")
data class Recipe(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    val isPublic: Boolean = false,

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val ingredients: MutableList<RecipeIngredient> = mutableListOf()
)