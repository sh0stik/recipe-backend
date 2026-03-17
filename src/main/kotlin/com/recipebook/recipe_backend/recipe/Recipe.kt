package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.category.Category
import com.recipebook.recipe_backend.note.RecipeNote
import com.recipebook.recipe_backend.user.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "recipes")
data class Recipe(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var isPublic: Boolean = false,

    @Column(name = "is_trashed", nullable = false)
    var isTrashed: Boolean = false,

    @Column(name = "copied_from_id")
    val copiedFromId: UUID? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val ingredients: MutableList<RecipeIngredient> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recipe_categories", // Matches your diagram table name
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: MutableList<Category> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recipe_shared_users",
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val sharedWith: MutableList<User> = mutableListOf(),

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    val notes: MutableList<RecipeNote> = mutableListOf()
)