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
    fun `remixes filter shows remixed recipes`() {
        val original = SampleRecipes.all.first()
        val remixed = original.remix(99, "Chili pasta", "Add chili flakes")
        val recipes = SampleRecipes.all + remixed

        val remixes = recipes.matching("", RecipeFilter.Remixes)
        assertEquals(listOf("Chili pasta"), remixes.map { it.name })
    }

    @Test
    fun `ingredient amount scales with servings`() {
        val ingredient = Ingredient(1.5, "cups", "flour")

        assertEquals("3 cups flour", ingredient.displayAmount(2.0))
        assertEquals("0.8 cups flour", ingredient.displayAmount(0.5))
    }

    @Test
    fun `remix copies method and adds twist extra ingredient and remix tag`() {
        val original = SampleRecipes.all.first { it.name == "Lemon Herb Pasta" }
        val remixed = original.remix(
            newId = 42,
            newName = "Spicy lemon pasta",
            twist = "Finish with chili flakes.",
            extraIngredient = Ingredient(1.0, "tsp", "chili flakes"),
        )

        assertEquals(42, remixed.id)
        assertEquals("Spicy lemon pasta", remixed.name)
        assertEquals("Lemon Herb Pasta", remixed.remixedFrom)
        assertTrue("Remix" in remixed.tags)
        assertTrue(original.tags.all { it in remixed.tags })
        assertEquals(original.steps + "Remix twist: Finish with chili flakes.", remixed.steps)
        assertEquals(original.ingredients + Ingredient(1.0, "tsp", "chili flakes"), remixed.ingredients)
        assertTrue(remixed.description.contains("Finish with chili flakes."))
        assertEquals(false, remixed.isFavorite)
    }
}
