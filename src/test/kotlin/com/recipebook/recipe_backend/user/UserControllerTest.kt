package com.recipebook.recipe_backend.user

import com.recipebook.recipe_backend.security.AuthService
import com.recipebook.recipe_backend.security.CurrentUserService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserControllerTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var authService: AuthService
    @Mock lateinit var currentUserService: CurrentUserService
    @Mock lateinit var httpServletRequest: HttpServletRequest

    private lateinit var controller: UserController

    @BeforeEach
    fun setUp() {
        controller = UserController(userRepository, authService, currentUserService, httpServletRequest)
    }

    @Test
    fun `createUser delegates to authService signUp`() {
        whenever(authService.signUp("user", "pass", "e@t.com")).thenReturn("jwt-token")

        val result = controller.createUser("user", "e@t.com", "pass")

        assertEquals("jwt-token", result)
    }

    @Test
    fun `login delegates to authService`() {
        whenever(authService.login("user", "pass")).thenReturn("jwt-token")

        assertEquals("jwt-token", controller.login("user", "pass"))
    }

    @Test
    fun `logout extracts token from Authorization header`() {
        whenever(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer my-jwt-token")
        whenever(authService.logout("my-jwt-token")).thenReturn(true)

        assertTrue(controller.logout())
        verify(authService).logout("my-jwt-token")
    }

    @Test
    fun `logout throws when no Authorization header`() {
        whenever(httpServletRequest.getHeader("Authorization")).thenReturn(null)

        assertThrows(Exception::class.java) {
            controller.logout()
        }
    }

    @Test
    fun `updateUsername succeeds when name is available`() {
        val user = User(id = UUID.randomUUID(), username = "old", email = "e@t.com", password = "p")
        whenever(currentUserService.getCurrentUser()).thenReturn(user)
        whenever(userRepository.findByUsername("newname")).thenReturn(Optional.empty())
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }

        val result = controller.updateUsername("newname")

        assertEquals("newname", result.username)
    }

    @Test
    fun `updateUsername throws when name is taken`() {
        val user = User(id = UUID.randomUUID(), username = "old", email = "e@t.com", password = "p")
        val existing = User(id = UUID.randomUUID(), username = "taken", email = "x@t.com", password = "p")
        whenever(currentUserService.getCurrentUser()).thenReturn(user)
        whenever(userRepository.findByUsername("taken")).thenReturn(Optional.of(existing))

        assertThrows(Exception::class.java) {
            controller.updateUsername("taken")
        }
    }

    @Test
    fun `user returns user by id`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, username = "chef", email = "c@t.com", password = "p")
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = controller.user(userId)

        assertNotNull(result)
        assertEquals("chef", result!!.username)
    }

    @Test
    fun `user returns null for missing id`() {
        whenever(userRepository.findById(any<UUID>())).thenReturn(Optional.empty())

        assertNull(controller.user(UUID.randomUUID()))
    }

    @Test
    fun `users returns all users`() {
        val users = listOf(
            User(username = "a", email = "a@t.com", password = "p"),
            User(username = "b", email = "b@t.com", password = "p")
        )
        whenever(userRepository.findAll()).thenReturn(users)

        assertEquals(2, controller.users().size)
    }
}
