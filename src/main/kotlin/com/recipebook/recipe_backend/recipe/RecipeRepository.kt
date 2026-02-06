package com.recipebook.recipe_backend.recipe

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RecipeRepository : JpaRepository<Recipe, UUID>{
    @Query("""
        SELECT r FROM Recipe r 
        JOIN r.user u 
        LEFT JOIN r.categories c 
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND r.isTrashed = false
    """)
    fun searchRecipes(
        @Param("keyword") keyword: String?,
        @Param("categoryId") categoryId: UUID?
    ): List<Recipe>
}