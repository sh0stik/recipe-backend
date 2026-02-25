package com.recipebook.recipe_backend.user

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "users")
data class User (@Id
                 @GeneratedValue(strategy = GenerationType.UUID)
                 val id: UUID? = null,

                 @Column(nullable = false, unique = true)
                 val username: String,

                 @Column(unique = true, nullable = false)
                 val email: String,

                 @CreationTimestamp
                 @Column(name = "created_at")
                 val createdAt: Instant? = null,

                 @UpdateTimestamp
                 @Column(name = "updated_at")
                 val updatedAt: Instant? = null,

                 @Column(nullable = false)
                 var password: String = "",

                 @Column(nullable = false)
                 val role: String = "ROLE_USER"
)