package com.recipebook.recipe_backend.category

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    // Self-Referencing relationship for "Parent Category"
    // e.g. "Cakes" (child) belongs to "Desserts" (parent)
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    val parentCategory: Category? = null
)
