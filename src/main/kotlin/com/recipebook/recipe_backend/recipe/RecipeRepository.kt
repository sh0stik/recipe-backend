package com.recipebook.recipe_backend.recipe

import com.recipebook.recipe_backend.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RecipeRepository : JpaRepository<Recipe, UUID>{
    @Query("""
        SELECT DISTINCT r FROM Recipe r
        JOIN r.user u
        LEFT JOIN r.categories c
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND r.isTrashed = false
        AND (r.isPublic = true OR r.user = :user)
    """)
    fun searchRecipes(
        @Param("keyword") keyword: String?,
        @Param("categoryId") categoryId: UUID?,
        @Param("user") user: User
    ): List<Recipe>

    @Query("""
        SELECT DISTINCT r FROM Recipe r
        LEFT JOIN r.sharedWith sw
        WHERE r.isTrashed = false
        AND (r.isPublic = true OR r.user = :user OR sw = :user)
    """)
    fun findVisibleRecipes(@Param("user") user: User): List<Recipe>
}