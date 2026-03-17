package com.recipebook.recipe_backend.security

import jakarta.persistence.*
import java.util.UUID
import java.time.Instant

@Entity
@Table(name = "blacklisted_tokens")
class BlacklistedToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false, length = 512)
    val token: String,

    @Column(nullable = false)
    val expiryDate: Instant
)