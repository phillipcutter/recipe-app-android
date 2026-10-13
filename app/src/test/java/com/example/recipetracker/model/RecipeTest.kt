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

    @Test
    fun `spicy remix adds heat ingredients and remix tag`() {
        val original = SampleRecipes.all.first { it.name == "Lemon Herb Pasta" }
        val remixed = original.remix(RemixStyle.Spicy)

        assertEquals("Lemon Herb Pasta (Spicy remix)", remixed.name)
        assertTrue(remixed.tags.contains("Remix"))
        assertTrue(remixed.tags.contains("Spicy"))
        assertTrue(remixed.ingredients.any { it.name.contains("chili") })
        assertEquals("Medium", remixed.difficulty)
        assertTrue(remixed.steps.any { it.contains("chili", ignoreCase = true) })
    }

    @Test
    fun `citrus remix merges extra lemon into existing lemon`() {
        val original = SampleRecipes.all.first { it.name == "Lemon Herb Pasta" }
        val remixed = original.remix(RemixStyle.Citrus)
        val lemon = remixed.ingredients.first { it.name == "lemon" }

        assertEquals(2.0, lemon.amount, 0.001)
        assertTrue(remixed.ingredients.any { it.name.contains("zest") })
    }

    @Test
    fun `remixes filter matches remixed recipes`() {
        val original = SampleRecipes.all.first()
        val remixed = original.copy(
            id = 99,
            name = original.remix(RemixStyle.Creamy).name,
            remixedFromId = original.id,
            tags = original.tags + "Remix",
        )
        val recipes = SampleRecipes.all + remixed

        assertEquals(listOf(remixed.name), recipes.matching("", RecipeFilter.Remixes).map { it.name })
    }
}
