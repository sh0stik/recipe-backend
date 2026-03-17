package com.recipebook.recipe_backend.security

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenCleanupService(
    private val blacklistedTokenRepository: BlacklistedTokenRepository
) {
    private val logger = LoggerFactory.getLogger(TokenCleanupService::class.java)

    // Cron expression: Seconds, Minutes, Hours, Day of month, Month, Day of week
    // "0 0 3 * * *" means: Run at exactly 3:00 AM every single day.
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        val now = Instant.now()
        val deletedCount = blacklistedTokenRepository.deleteTokensExpiredBefore(now)

        logger.info("Automated Job: Deleted {} expired tokens from the database.", deletedCount)
    }
}