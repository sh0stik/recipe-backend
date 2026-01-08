package com.recipebook.recipe_backend.ingredient

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "ingredients")
data class Ingredient(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val name: String
)