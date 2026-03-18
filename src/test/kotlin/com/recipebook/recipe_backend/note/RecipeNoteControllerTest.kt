package com.recipebook.recipe_backend.note

import com.recipebook.recipe_backend.recipe.Recipe
import com.recipebook.recipe_backend.recipe.RecipeRepository
import com.recipebook.recipe_backend.security.CurrentUserService
import com.recipebook.recipe_backend.user.User
import com.recipebook.recipe_backend.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class RecipeNoteControllerTest {

    @Mock lateinit var recipeNoteRepository: RecipeNoteRepository
    @Mock lateinit var recipeRepository: RecipeRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var currentUserService: CurrentUserService

    @Test
    fun `addNote creates note linked to recipe and current user`() {
        val user = User(id = UUID.randomUUID(), username = "chef", email = "c@t.com", password = "p")
        val recipeId = UUID.randomUUID()
        val recipe = Recipe(id = recipeId, name = "Pasta", user = user)

        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe))
        whenever(currentUserService.getCurrentUser()).thenReturn(user)
        whenever(recipeNoteRepository.save(any<RecipeNote>())).thenAnswer { it.arguments[0] }

        val controller = RecipeNoteController(recipeNoteRepository, recipeRepository, userRepository, currentUserService)
        val result = controller.addNote(recipeId, "Great recipe!")

        assertEquals("Great recipe!", result.note)
        assertEquals(user, result.user)
        assertEquals(recipe, result.recipe)
    }

    @Test
    fun `addNote throws when recipe not found`() {
        whenever(recipeRepository.findById(any<UUID>())).thenReturn(Optional.empty())

        val controller = RecipeNoteController(recipeNoteRepository, recipeRepository, userRepository, currentUserService)

        assertThrows(Exception::class.java) {
            controller.addNote(UUID.randomUUID(), "Note")
        }
    }
}
