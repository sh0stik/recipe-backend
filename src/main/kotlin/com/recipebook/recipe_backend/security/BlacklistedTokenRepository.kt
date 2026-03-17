package com.recipebook.recipe_backend.security
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface BlacklistedTokenRepository : JpaRepository<BlacklistedToken, UUID> {
    fun existsByToken(token: String): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken b WHERE b.expiryDate < :now")
    fun deleteTokensExpiredBefore(now: Instant): Int
}