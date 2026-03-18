package com.recipebook.recipe_backend.security

import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var passwordEncoder: PasswordEncoder
    @Mock lateinit var jwtService: JwtService
    @Mock lateinit var authenticationManager: AuthenticationManager
    @Mock lateinit var passwordResetTokenRepository: PasswordResetTokenRepository
    @Mock lateinit var emailService: EmailService
    @Mock lateinit var blacklistedTokenRepository: BlacklistedTokenRepository

    private lateinit var authService: AuthService

    private val testUser = User(
        id = UUID.randomUUID(),
        username = "testuser",
        email = "test@test.com",
        password = "encoded_pass",
        role = "ROLE_USER"
    )

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            userRepository, passwordEncoder, jwtService,
            authenticationManager, passwordResetTokenRepository,
            emailService, blacklistedTokenRepository
        )
    }

    @Test
    fun `signUp succeeds with new username`() {
        whenever(userRepository.findByUsername("newuser")).thenReturn(Optional.empty())
        whenever(passwordEncoder.encode("password")).thenReturn("encoded_pass")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }
        whenever(jwtService.generateToken(any<CustomUserDetails>())).thenReturn("jwt-token")

        val token = authService.signUp("newuser", "password", "new@test.com")

        assertEquals("jwt-token", token)
        verify(userRepository).save(any<User>())
        verify(jwtService).generateToken(any<CustomUserDetails>())
    }

    @Test
    fun `signUp throws when username is taken`() {
        whenever(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser))

        val ex = assertThrows(Exception::class.java) {
            authService.signUp("testuser", "password", "test@test.com")
        }
        assertEquals("Username already taken: testuser", ex.message)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `login returns token on valid credentials`() {
        whenever(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser))
        whenever(jwtService.generateToken(any<CustomUserDetails>())).thenReturn("jwt-token")

        val token = authService.login("testuser", "password")

        assertEquals("jwt-token", token)
        verify(authenticationManager).authenticate(any<UsernamePasswordAuthenticationToken>())
    }

    @Test
    fun `login throws when user not found`() {
        whenever(authenticationManager.authenticate(any())).thenReturn(null)
        whenever(userRepository.findByUsername("ghost")).thenReturn(Optional.empty())

        assertThrows(Exception::class.java) {
            authService.login("ghost", "password")
        }
    }

    @Test
    fun `requestPasswordReset returns true even if email not found`() {
        whenever(userRepository.findUserByEmail("unknown@test.com")).thenReturn(Optional.empty())

        assertTrue(authService.requestPasswordReset("unknown@test.com"))
        verify(emailService, never()).sendPasswordResetEmail(any(), any())
    }

    @Test
    fun `requestPasswordReset sends email when user exists`() {
        whenever(userRepository.findUserByEmail("test@test.com")).thenReturn(Optional.of(testUser))
        whenever(passwordResetTokenRepository.save(any<PasswordResetToken>())).thenAnswer { it.arguments[0] }

        assertTrue(authService.requestPasswordReset("test@test.com"))
        verify(emailService).sendPasswordResetEmail(eq("test@test.com"), any())
        verify(passwordResetTokenRepository).save(any<PasswordResetToken>())
    }

    @Test
    fun `resetPassword returns false for invalid token`() {
        whenever(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty())

        assertFalse(authService.resetPassword("bad-token", "newpass"))
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `resetPassword returns false for expired token`() {
        val expiredToken = PasswordResetToken(
            token = "expired",
            expiryDate = Instant.now().minusSeconds(600),
            user = testUser
        )
        whenever(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken))

        assertFalse(authService.resetPassword("expired", "newpass"))
        verify(passwordResetTokenRepository).delete(expiredToken)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `resetPassword succeeds with valid token`() {
        val validToken = PasswordResetToken(
            token = "valid",
            expiryDate = Instant.now().plusSeconds(600),
            user = testUser
        )
        whenever(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(validToken))
        whenever(passwordEncoder.encode("newpass")).thenReturn("encoded_new")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }

        assertTrue(authService.resetPassword("valid", "newpass"))
        assertEquals("encoded_new", testUser.password)
        verify(passwordResetTokenRepository).delete(validToken)
    }

    @Test
    fun `logout blacklists token`() {
        whenever(blacklistedTokenRepository.existsByToken("jwt")).thenReturn(false)
        whenever(jwtService.extractExpiration("jwt")).thenReturn(Date())
        whenever(blacklistedTokenRepository.save(any<BlacklistedToken>())).thenAnswer { it.arguments[0] }

        assertTrue(authService.logout("jwt"))
        verify(blacklistedTokenRepository).save(any<BlacklistedToken>())
    }

    @Test
    fun `logout returns true if token already blacklisted`() {
        whenever(blacklistedTokenRepository.existsByToken("jwt")).thenReturn(true)

        assertTrue(authService.logout("jwt"))
        verify(blacklistedTokenRepository, never()).save(any())
    }
}
