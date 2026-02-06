package com.recipebook.recipe_backend.trash

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "recipe_trash")
data class RecipeTrash (
    @Id
    @Column(name = "recipe_id")
    val recipeId: UUID,

    @Column(name = "deleted_at")
    val deletedAt: Instant = Instant.now()
    )