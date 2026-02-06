package com.recipebook.recipe_backend.trash

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeTrashRepository : JpaRepository<RecipeTrash, UUID>