package com.recipebook.recipe_backend.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class TokenCleanupServiceTest {

    @Mock lateinit var blacklistedTokenRepository: BlacklistedTokenRepository

    @Test
    fun `cleanUpExpiredTokens calls repository delete`() {
        whenever(blacklistedTokenRepository.deleteTokensExpiredBefore(any())).thenReturn(5)

        val service = TokenCleanupService(blacklistedTokenRepository)
        service.cleanUpExpiredTokens()

        verify(blacklistedTokenRepository).deleteTokensExpiredBefore(any())
    }
}
