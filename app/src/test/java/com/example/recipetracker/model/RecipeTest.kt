package com.example.recipetracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTest {
    @Test
    fun `search matches names ingredients and tags`() {
        val recipes = SampleRecipes.all

        assertEquals(listOf("Ginger Miso Salmon"), recipes.matching("ginger", RecipeFilter.All).map { it.name })
        assertTrue(recipes.matching("vegetarian", RecipeFilter.All).size >= 2)
        assertEquals(listOf("Lemon Herb Pasta"), recipes.matching("pasta", RecipeFilter.All).map { it.name })
    }

    @Test
    fun `filters favorites and quick recipes`() {
        val recipes = SampleRecipes.all

        assertTrue(recipes.matching("", RecipeFilter.Favorites).all { it.isFavorite })
        assertTrue(recipes.matching("", RecipeFilter.Quick).all { it.prepMinutes <= 30 })
    }

    @Test
    fun `ingredient amount scales with servings`() {
        val ingredient = Ingredient(1.5, "cups", "flour")

        assertEquals("3 cups flour", ingredient.displayAmount(2.0))
        assertEquals("0.8 cups flour", ingredient.displayAmount(0.5))
    }
}
